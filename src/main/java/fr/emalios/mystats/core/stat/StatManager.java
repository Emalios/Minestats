package fr.emalios.mystats.core.stat;

import fr.emalios.mystats.core.dao.InventoryDao;
import fr.emalios.mystats.core.dao.InventorySnapshotDao;
import fr.emalios.mystats.core.dao.SnapshotItemDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.db.DatabaseWorker;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class StatManager {

    private static StatManager instance;

    private final int BATCH_SIZE = 100;
    private final int FLUSH_INTERVAL_SECONDS = 5;

    private final Database db = Database.getInstance();
    private final InventorySnapshotDao inventorySnapshotDao = db.getInventorySnapshotDao();
    private final InventoryDao inventoryDao = db.getInventoryDao();
    private final SnapshotItemDao snapshotItemDao = db.getSnapshotItemDao();
    private final Map<Integer, BlockCapabilityCache<IItemHandler,Direction>> monitored = new HashMap<>();
    private final Queue<Stat> buffer = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "MineStats-Batch-Flusher");
                t.setDaemon(true);
                return t;
            });

    private StatManager() {
        this.scheduler.scheduleAtFixedRate(this::flush, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public static synchronized StatManager getInstance() {
        if (instance == null) {
            instance = new StatManager();
        }
        return instance;
    }

    public void shutdown() {
        this.scheduler.shutdown();
    }

    public void add(int inventoryId, BlockCapabilityCache<IItemHandler,Direction> handler) {
        this.monitored.put(inventoryId, handler);
    }

    public boolean isMonitored(int inventoryId) {
        return this.monitored.containsKey(inventoryId);
    }

    /**
     * TODO: maybe a system to detect if the inventory has changed since last scan is possible
     * Method responsible to scan the content of every monitored blocks
     * for each block we:
     * - create an inventory snapshot
     * - scan inventory content
     * - for each item create a snapshot item
     */
    public void scan() throws SQLException {
        for (Integer inventoryId : this.monitored.keySet()) {
            //create snapshot
            int snapshotId = this.inventorySnapshotDao.insert(inventoryId, Instant.now().getEpochSecond());
            //check if inventory still exists
            IItemHandler handler = this.monitored.get(inventoryId).getCapability();
            if(handler == null) {
                this.unmonitore(inventoryId);
                continue;
            };
            //get inventory content
            var content = Utils.getInventoryContent(handler);
            //put everything in db
            for (Map.Entry<String, Integer> entry : content.entrySet()) {
                String s = entry.getKey();
                Integer integer = entry.getValue();
                this.snapshotItemDao.insert(snapshotId, s, integer);
            }
        }
    }

    public void unmonitore(int inventoryId) throws SQLException {
        this.monitored.remove(inventoryId);
        this.inventoryDao.deleteById(inventoryId);
    }

    //load inventories in database
    public void init(MinecraftServer server) throws SQLException {

        server.getAllLevels().forEach(level -> {
            System.out.println(level.dimension().location());
        });
    }

    //IBlockExtension: getCloneItemStack

    private synchronized void flush() {
        if (buffer.isEmpty()) return;

        System.out.println("FLUSH");
        // drain to array
        Stat[] entries = buffer.toArray(Stat[]::new);
        buffer.clear();

        DatabaseWorker.submitBatch(entries);
    }
}
