package fr.emalios.mystats.core.db;

import fr.emalios.mystats.core.stat.Stat;
import fr.emalios.mystats.core.stat.Unit;
import fr.emalios.mystats.core.stat.StatType;

import java.sql.*;
import java.time.Instant;
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

    private static Connection connection;

    /**
     * Initialise la base SQLite et crée les tables si nécessaire.
     */
    public static void init() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:mystats.db");
            connection.setAutoCommit(false); // batch mode

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS stats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL,
                        target_id TEXT NOT NULL,
                        owner_uuid TEXT,
                        source_id TEXT,
                        count REAL NOT NULL,
                        unit TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """);
            }

            connection.commit();

            System.out.println("[MyStats] Database initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Soumet un batch d'entrées à insérer asynchronement.
     */
    public static void submitBatch(Stat[] entries) {
        if (entries == null || entries.length == 0) return;
        EXECUTOR.submit(() -> insertBatch(entries));
    }

    /**
     * Insère un batch dans la base SQLite.
     */
    private static void insertBatch(Stat[] entries) {
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
            try {
                connection.rollback();
            } catch (SQLException ignored) {}
            System.err.println("[MyStats] Database batch insert failed: " + e.getMessage());
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
