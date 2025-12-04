package fr.emalios.mystats.core.db;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.core.dao.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
interface SQLConsumer {
    void accept(Connection conn) throws SQLException;
}

@FunctionalInterface
interface SQLFunction<T> {
    T apply(Connection conn) throws SQLException;
}

public final class Database {

    private static Database instance;
    private Connection connection;

    private ExecutorService dbExecutor;

    //dao
    private InventorySnapshotDao inventorySnapshotDao;
    private PlayerDao playerDao;
    private PlayerInventoryDao playerInventoryDao;
    private SnapshotItemDao snapshotItemDao;
    private InventoryDao inventoryDao;

    private Database() throws SQLException {
        this.init();
        this.inventoryDao = new InventoryDao(connection);
        this.inventorySnapshotDao = new InventorySnapshotDao(connection);
        this.playerDao = new PlayerDao(connection);
        this.playerInventoryDao = new PlayerInventoryDao(connection);
        this.snapshotItemDao = new SnapshotItemDao(connection);
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            try {
                instance = new Database();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
        return instance;
    }

    public void init() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:mystats.db");

            try (Statement st = this.connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
                st.execute("PRAGMA synchronous=NORMAL;");
                st.execute("PRAGMA busy_timeout=5000;");
            }

            this.connection.setAutoCommit(false);
            this.connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "MyStats-DB-Thread");
            return t;
        });
    }

    public Connection getConnection() {
        return connection;
    }

    public void executeWriteAsync(SQLConsumer operation) {
        dbExecutor.submit(() -> {
            try {
                operation.accept(connection);
                connection.commit();
            } catch (SQLException e) {
                logDatabaseBusy(e, "<write operation>");
            }
        });
    }

    public <T> CompletableFuture<T> executeQueryAsync(SQLFunction<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.apply(connection);
            } catch (SQLException e) {
                logDatabaseBusy(e, "<query operation>");
                throw new RuntimeException(e);
            }
        }, dbExecutor);
    }

    public void deleteDatabaseAsync() {

    }

    public void close() {
        dbExecutor.shutdown();
        try {
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
        } catch (InterruptedException ignored) {}

        try {
            if (connection != null) connection.close();
        } catch (Exception ignored) {}
    }

    public InventorySnapshotDao getInventorySnapshotDao() {
        return inventorySnapshotDao;
    }

    public PlayerDao getPlayerDao() {
        return playerDao;
    }

    public PlayerInventoryDao getPlayerInventoryDao() {
        return playerInventoryDao;
    }

    public SnapshotItemDao getSnapshotItemDao() {
        return snapshotItemDao;
    }

    public InventoryDao getInventoryDao() {
        return inventoryDao;
    }

    public static void logDatabaseBusy(SQLException e, String sql) {
        if (!e.getMessage().contains("SQLITE_BUSY")) return;

        System.err.println("========== SQLITE BUSY DETECTED ==========");
        System.err.println("Thread: " + Thread.currentThread().getName());
        System.err.println("While executing SQL: " + sql);
        e.printStackTrace();
        System.err.println("==========================================");
    }
}
