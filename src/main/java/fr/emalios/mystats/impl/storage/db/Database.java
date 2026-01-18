package fr.emalios.mystats.impl.storage.db;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.impl.adapter.McHandlersLoader;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.db.migrations.Migration;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;
import fr.emalios.mystats.impl.storage.repository.*;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void openConnection(String dbFileName) throws SQLException {
        if(connection != null && !connection.isClosed()) throw new RuntimeException("Connection already open");
        //MyStats.LOGGER.info("[Minestats] Opening DB connection.");
        this.connection = DriverManager.getConnection("jdbc:sqlite:"+dbFileName);
    }

    private void initDaos() {
        this.inventoryDao = new InventoryDao(connection);
        this.inventorySnapshotDao = new InventorySnapshotDao(connection);
        this.playerDao = new PlayerDao(connection);
        this.inventoryPositionsDao = new InventoryPositionsDao(connection);
        this.playerInventoryDao = new PlayerInventoryDao(connection);
        this.recordDao = new RecordDao(connection);
    }

    /**
     * Initialize the database and make migrations. Assume migrations are already loaded
     * @param dbFileName file name of the db
     * @param logger logger to send output log
     */
    public void init(String dbFileName, Logger logger) {
        try {
            this.openConnection(dbFileName);
            this.initDaos();
            this.makeMigrations(logger);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        try {
            this.inventoryDao.deleteAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeMigrations(Logger logger) {
        try {
            MigrationRunner.migrate(this.connection, this.migrations, logger);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Migration> getMigrations() {
        Collections.sort(this.migrations);
        return migrations;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                //MyStats.LOGGER.info("[Minestats] Closing DB connection.");
                connection.close();
            }
        } catch (Exception ignored) {}
    }

    public void registerMigration(Migration migration) {
        if(!this.migrations.contains(migration)) this.migrations.add(migration);
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

}
