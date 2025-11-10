package fr.emalios.mystats.core.stat;

import fr.emalios.mystats.core.db.DatabaseWorker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatManager {

    private static final int BATCH_SIZE = 100;
    private static final int FLUSH_INTERVAL_SECONDS = 5;

    private static final Queue<Stat> buffer = new ConcurrentLinkedQueue<>();
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "MineStats-Batch-Flusher");
                t.setDaemon(true);
                return t;
            });

    public static void init() {
        scheduler.scheduleAtFixedRate(StatManager::flush, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public static void shutdown() {
        flush();
        scheduler.shutdown();
    }

    public static void log(Stat entry) {
        buffer.add(entry);
        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    private static synchronized void flush() {
        if (buffer.isEmpty()) return;

        System.out.println("FLUSH");
        // drain to array
        Stat[] entries = buffer.toArray(Stat[]::new);
        buffer.clear();

        DatabaseWorker.submitBatch(entries);
    }
}
