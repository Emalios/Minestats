package fr.emalios.mystats.core.db;

import fr.emalios.mystats.core.stat.Record;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Worker asynchrone responsable de la persistance des statistiques en base SQLite.
 * Utilise un thread unique pour sérialiser les écritures et éviter la contention.
 */
public class DatabaseWorker {

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "MyStats-DB-Worker");
                t.setDaemon(true);
                return t;
            });


    public static void submitBatch(Record[] entries) {
        if (entries == null || entries.length == 0) return;
        EXECUTOR.submit(() -> insertBatch(entries));
    }

    private static void insertBatch(Record[] entries) {

    }

    /**
     * Ferme proprement le worker et la connexion.
     */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException ignored) {}
    }
}
