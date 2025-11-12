package fr.emalios.mystats.core.stat;

import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.db.DatabaseWorker;
import net.neoforged.neoforge.items.IItemHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatManager {

    private static StatManager instance;

    private final int BATCH_SIZE = 100;
    private final int FLUSH_INTERVAL_SECONDS = 5;

    private final List<IItemHandler> monitored = new ArrayList<>();
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
        flush();
        this.scheduler.shutdown();
    }

    /**
     * TODO: maybe a system to detect if the inventory has changed since last scan is possible
     * Method responsible to scan the content of every monitored blocks
     * for each block we:
     * - create an inventory snapshot
     * - scan inventory content
     * - for each item create a snapshot item
     */
    public void scan() {

    }

    public void log(Stat entry) {
        buffer.add(entry);
        if (buffer.size() >= BATCH_SIZE) {
            flush();
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
