package fr.emalios.mystats.core.stat;

import fr.emalios.mystats.core.SnapshotDao;
import fr.emalios.mystats.core.dao.InventorySnapshotDao;
import fr.emalios.mystats.core.dao.SnapshotItemDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.db.DatabaseWorker;
import fr.emalios.mystats.helper.Utils;
import net.neoforged.neoforge.items.IItemHandler;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatManager {

    private static StatManager instance;

    private final int BATCH_SIZE = 100;
    private final int FLUSH_INTERVAL_SECONDS = 5;

    private final Database db = Database.getInstance();
    private final InventorySnapshotDao inventorySnapshotDao = db.getInventorySnapshotDao();
    private final SnapshotItemDao snapshotItemDao = db.getSnapshotItemDao();
    private final Map<Integer, IItemHandler> monitored = new HashMap<>();
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

    public void add(int inventoryId, IItemHandler handler) {
        this.monitored.put(inventoryId, handler);
    }

    public boolean isMonitored(int inventoryId) {
        return this.monitored.containsKey(inventoryId);
    }

    public void remove(int inventoryId) {
        this.monitored.remove(inventoryId);
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
            //get inventory content
            var content = Utils.getInventoryContent(this.monitored.get(inventoryId));
            //put everything in db
            for (Map.Entry<String, Integer> entry : content.entrySet()) {
                String s = entry.getKey();
                Integer integer = entry.getValue();
                this.snapshotItemDao.insert(snapshotId, s, integer);
            }
        }
    }

    private synchronized void flush() {
        if (buffer.isEmpty()) return;

        System.out.println("FLUSH");
        // drain to array
        Stat[] entries = buffer.toArray(Stat[]::new);
        buffer.clear();

        DatabaseWorker.submitBatch(entries);
    }
}
