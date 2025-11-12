package fr.emalios.mystats.core.db;

import fr.emalios.mystats.core.dao.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static Database instance;
    private final Connection connection;

    //dao
    private final InventoryDao inventoryDao;
    private final InventorySnapshotDao inventorySnapshotDao;
    private final PlayerDao playerDao;
    private final PlayerInventoryDao playerInventoryDao;
    private final SnapshotItemDao snapshotItemDao;

    private Database() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:mystats.db");
        this.connection.setAutoCommit(false);

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

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (!connection.isClosed()) connection.close();
        } catch (SQLException e) {
            System.err.println("[MyStats] Failed to close database: " + e.getMessage());
        }
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
}
