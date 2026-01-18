package fr.emalios.mystats.impl.storage.db;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.impl.adapter.McHandlersLoader;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.db.migrations.Migration;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;
import fr.emalios.mystats.impl.storage.repository.*;
import net.minecraft.server.MinecraftServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class Database {

    private static Database instance;
    private Connection connection;

    private final List<Migration> migrations = new ArrayList<>();

    //dao
    private InventorySnapshotDao inventorySnapshotDao;
    private InventoryPositionsDao inventoryPositionsDao;
    private PlayerDao playerDao;
    private PlayerInventoryDao playerInventoryDao;
    private RecordDao recordDao;
    private InventoryDao inventoryDao;

    /**
     * SHOULD ONLY BE USED FOR TESTS PURPOSES
     */
    private Database() {
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void openConnection(String dbFileName) throws SQLException {
        if(connection != null && !connection.isClosed()) throw new RuntimeException("Connection already open");
        MyStats.LOGGER.info("[Minestats] Opening DB connection.");
        this.connection = DriverManager.getConnection("jdbc:sqlite:"+dbFileName+".db");
    }

    private void initDaos() {
        this.inventoryDao = new InventoryDao(connection);
        this.inventorySnapshotDao = new InventorySnapshotDao(connection);
        this.playerDao = new PlayerDao(connection);
        this.inventoryPositionsDao = new InventoryPositionsDao(connection);
        this.playerInventoryDao = new PlayerInventoryDao(connection);
        this.recordDao = new RecordDao(connection);
    }

    public void init(String dbFileName, MinecraftServer server) {
        try {
            this.openConnection(dbFileName);
            this.initDaos();
            this.updateRepositories(server);
            this.makeMigrations();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateRepositories(MinecraftServer server) {
        var playerInvRepo = new SqlitePlayerInventoryRepository(Database.getInstance().getPlayerInventoryDao());
        StatsAPI.getInstance().init(new SqlitePlayerRepository(Database.getInstance().getPlayerDao(), playerInvRepo),
                new SqliteInventoryRepository(Database.getInstance().getInventoryDao(), Database.getInstance().getInventoryPositionsDao()),
                playerInvRepo,
                new SqliteInventorySnapshotRepository(
                        Database.getInstance().getInventorySnapshotDao(),
                        Database.getInstance().getRecordDao()),
                new SqliteInventoryPositionsRepository(Database.getInstance().getInventoryPositionsDao()),
                new McHandlersLoader(server)
        );
    }

    public void reset() {
        try {
            this.inventoryDao.deleteAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeMigrations() {
        try {
            MigrationRunner.migrate(this.connection, this.migrations, MyStats.LOGGER);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                MyStats.LOGGER.info("[Minestats] Closing DB connection.");
                connection.close();
            }
        } catch (Exception ignored) {}
    }

    public void registerMigration(Migration migration) {
        this.migrations.add(migration);
    }

    public void resetMigrations() {
        this.migrations.clear();
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

    public RecordDao getRecordDao() {
        return recordDao;
    }

    public InventoryDao getInventoryDao() {
        return inventoryDao;
    }

    public InventoryPositionsDao getInventoryPositionsDao() {
        return inventoryPositionsDao;
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
