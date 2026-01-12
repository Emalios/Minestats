package minestats.api.storage.migrations;

import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.migrations.Migration;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;
import minestats.api.storage.DatabaseTest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static minestats.utils.Const.pathToMigrations;

@DisplayName("Migration's tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MigrationTest {

    private static final int lastMigrationVersion = 2;

    private List<Migration> migrations;
    private Connection connection;

    @BeforeAll
    void beforeAll() throws SQLException {
        migrations = new MigrationLoader(pathToMigrations).loadAll();
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
    }

    @AfterAll
    void teardown() throws SQLException {
        connection.close();
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Init is version 0")
    public void initIsVersion0() throws SQLException {
        Assertions.assertEquals(0, MigrationRunner.getCurrentVersion(connection));
    }

    @Test
    @DisplayName("Do migration 1")
    public void doMigration1() throws SQLException {
        Assertions.assertEquals(0, MigrationRunner.getCurrentVersion(connection));
        MigrationRunner.applyMigration(connection, migrations.getFirst());
        Assertions.assertEquals(1, MigrationRunner.getCurrentVersion(connection));
        Assertions.assertTrue(tableExists(connection, "players"));
        Assertions.assertTrue(tableExists(connection, "inventories"));
        Assertions.assertTrue(tableExists(connection, "inventory_snapshots"));
        Assertions.assertTrue(tableExists(connection, "snapshot_items"));
        Assertions.assertFalse(tableExists(connection, "inventory_pos"));
        Assertions.assertFalse(tableExists(connection, "inventory_new"));
    }

    @Test
    @DisplayName("Do migration 2")
    public void doMigration2() throws SQLException {
        Assertions.assertEquals(1, MigrationRunner.getCurrentVersion(connection));
        MigrationRunner.applyMigration(connection, migrations.get(1));
        Assertions.assertEquals(2, MigrationRunner.getCurrentVersion(connection));
        Assertions.assertTrue(tableExists(connection, "players"));
        Assertions.assertTrue(tableExists(connection, "inventories"));
        Assertions.assertTrue(tableExists(connection, "inventory_snapshots"));
        Assertions.assertTrue(tableExists(connection, "snapshot_items"));
        Assertions.assertTrue(tableExists(connection, "inventory_pos"));
        Assertions.assertFalse(tableExists(connection, "inventory_new"));
    }

    @Test
    @DisplayName("Do all migrations")
    public void doAllMigrations() throws SQLException, IOException {
        var dbConnection = DatabaseTest.getConnection();
        Assertions.assertEquals(0, MigrationRunner.getCurrentVersion(dbConnection));
        DatabaseTest.makeMigrations();
        Assertions.assertEquals(lastMigrationVersion, MigrationRunner.getCurrentVersion(dbConnection));
        Assertions.assertTrue(tableExists(dbConnection, "players"));
        Assertions.assertTrue(tableExists(dbConnection, "inventories"));
        Assertions.assertTrue(tableExists(dbConnection, "inventory_snapshots"));
        Assertions.assertTrue(tableExists(dbConnection, "snapshot_items"));
        Assertions.assertTrue(tableExists(dbConnection, "inventory_pos"));
        Assertions.assertFalse(tableExists(dbConnection, "inventory_new"));
    }

    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (var rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

}
