package fr.emalios.mystats.core.db;

import fr.emalios.mystats.core.stat.Stat;
import org.sqlite.core.DB;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Worker asynchrone responsable de la persistance des statistiques en base SQLite.
 * Utilise un thread unique pour sérialiser les écritures et éviter la contention.
 */
public class DatabaseWorker {

    private static final ReentrantLock DB_LOCK = new ReentrantLock();

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "MyStats-DB-Worker");
                t.setDaemon(true);
                return t;
            });

    private static Connection connection;

    public static void init() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:mystats.db");
            connection.setAutoCommit(false); // nécessaire pour batch mode
            DB_LOCK.lock();
            DatabaseManager.createTables(connection);
            DB_LOCK.unlock();
            System.out.println("[MyStats] Database initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static void submitBatch(Stat[] entries) {
        if (entries == null || entries.length == 0) return;
        EXECUTOR.submit(() -> insertBatch(entries));
    }

    public static void showDbStructure() {
        DB_LOCK.lock();
        DatabaseManager.showDbStructures(connection);
        DB_LOCK.unlock();
    }

    public static void deleteDb() {
        //DB_LOCK.lock();
        try {
            DatabaseManager.deleteDb(connection);
        } finally {
            //DB_LOCK.unlock();
        }
    }

    private static void insertBatch(Stat[] entries) {
        DB_LOCK.lock();
        try {
            String sql = """
            INSERT INTO stats (type, target_id, owner_uuid, source_id, count, unit, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Stat entry : entries) {
                    ps.setString(1, entry.getType().name());
                    ps.setString(2, entry.getTargetId());
                    ps.setString(3, entry.getOwnerId() != null ? entry.getOwnerId().toString() : null);
                    ps.setString(4, entry.getSourceId());
                    ps.setFloat(5, entry.getCount());
                    ps.setString(6, entry.getUnit().name());
                    ps.setLong(7, entry.getTimestamp().getEpochSecond());
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                System.err.println("[MyStats] Database batch insert failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB_LOCK.unlock();
        }
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

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[MyStats] Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[MyStats] Failed to close database: " + e.getMessage());
        }
    }
}
