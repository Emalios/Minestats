package minestats.api.storage.migrations;

import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.migrations.Migration;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static minestats.utils.Const.pathToMigrations;

@DisplayName("Test migration 1 to 2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Migration1To2Test {

    private List<Migration> migrations;
    private Database database;

    @BeforeAll
    void beforeAll() {
        database = new Database();
        database.init(":memory:", LoggerFactory.getLogger(Migration1To2Test.class));
        migrations = new MigrationLoader(pathToMigrations).loadAll();
    }

    @AfterAll
    void afterAll() {
        database.close();
    }


    @Test
    @DisplayName("Migration V1 -> V2 keeps inventory and creates inventory_pos")
    public void migrationV1toV2Test() throws SQLException {
        Connection conn = database.getConnection();

        Assertions.assertEquals(0, MigrationRunner.getCurrentVersion(conn));
        MigrationRunner.applyMigration(conn, migrations.get(0));
        Assertions.assertEquals(1, MigrationRunner.getCurrentVersion(conn));

        int inventoryId;
        try (var stmt = conn.prepareStatement(
                "INSERT INTO inventories(world, x, y, z, type) VALUES (?, ?, ?, ?, ?)",
                java.sql.Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setString(1, "world");
            stmt.setInt(2, 0);
            stmt.setInt(3, 64);
            stmt.setInt(4, 0);
            stmt.setString(5, "UNUSED");
            stmt.executeUpdate();

            try (var rs = stmt.getGeneratedKeys()) {
                Assertions.assertTrue(rs.next());
                inventoryId = rs.getInt(1);
            }
        }

        try (var stmt = conn.prepareStatement(
                "SELECT * FROM inventories WHERE id = ?"
        )) {
            stmt.setInt(1, inventoryId);
            try (var rs = stmt.executeQuery()) {
                Assertions.assertTrue(rs.next(), "Inventory should exist in V1");
            }
        }

        MigrationRunner.applyMigration(conn, migrations.get(1));
        Assertions.assertEquals(2, MigrationRunner.getCurrentVersion(conn));

        try (var stmt = conn.prepareStatement(
                "SELECT * FROM inventories WHERE id = ?"
        )) {
            stmt.setInt(1, inventoryId);
            try (var rs = stmt.executeQuery()) {
                Assertions.assertTrue(rs.next(), "Inventory should still exist after migration to V2");
            }
        }

        try (var stmt = conn.prepareStatement(
                "SELECT * FROM inventory_pos"
        )) {
            //stmt.setInt(1, inventoryId);
            try (var rs = stmt.executeQuery()) {
                Assertions.assertTrue(rs.next(), "Inventory_pos entry should exist after migration");
                Assertions.assertEquals("world", rs.getString("world"));
                Assertions.assertEquals(0, rs.getInt("x"));
                Assertions.assertEquals(64, rs.getInt("y"));
                Assertions.assertEquals(0, rs.getInt("z"));
            }
        }
    }



}
