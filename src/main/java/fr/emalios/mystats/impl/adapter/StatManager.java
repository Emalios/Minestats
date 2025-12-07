package fr.emalios.mystats.impl.adapter;

import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;
import fr.emalios.mystats.impl.storage.dao.InventorySnapshotDao;
import fr.emalios.mystats.impl.storage.dao.RecordDao;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StatManager {

    private static StatManager instance;

    private final int BATCH_SIZE = 100;
    private final int FLUSH_INTERVAL_SECONDS = 5;

    private final Database db = Database.getInstance();
    private final InventorySnapshotDao inventorySnapshotDao = db.getInventorySnapshotDao();
    private final InventoryDao inventoryDao = db.getInventoryDao();
    private final RecordDao recordDao = db.getSnapshotItemDao();

    //link an inventoryId to associated capability handlers
    //might be necessary to use coordinates/level here
    private final Map<Integer, List<IHandler>> monitored = new HashMap<>();
    private final Queue<fr.emalios.mystats.api.stat.Record> buffer = new ConcurrentLinkedQueue<>();

    private StatManager() {
    }

    public static synchronized StatManager getInstance() {
        if (instance == null) {
            instance = new StatManager();
        }
        return instance;
    }

    public void add(int inventoryId, List<IHandler> handlers) {
        this.monitored.put(inventoryId, handlers);
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
            List<IHandler> handlers = this.monitored.get(inventoryId);
            if(handlers.isEmpty() || !handlers.get(0).exists()) {
                System.out.println("unmonitore");
                System.out.println(handlers.get(0));
                this.unmonitore(inventoryId);
                continue;
            }
            //get inventory content
            for (IHandler handler : handlers) {
                this.recordDao.insert(snapshotId, handler.getContent());
            }
        }
    }

    public void unmonitore(int inventoryId) throws SQLException {
        this.monitored.remove(inventoryId);
        this.inventoryDao.deleteById(inventoryId);
    }

    //load inventories in database
    public void init(MinecraftServer server) throws SQLException {
        Map<String, ServerLevel> levels = new HashMap<>();
        server.getAllLevels().forEach(level -> {
            levels.put(level.dimension().location().toString(), level);
        });
        for (var inventory : this.inventoryDao.findAll()) {
            int invId = inventory.id();
            List<IHandler> handlers = Utils.getIHandlers(
                    levels.get(inventory.world()),
                    new BlockPos(inventory.x(), inventory.y(), inventory.z()));

            if (handlers.isEmpty()) {
                this.unmonitore(invId);
                continue;
            }
            this.add(invId, handlers);
        }
    }
}
