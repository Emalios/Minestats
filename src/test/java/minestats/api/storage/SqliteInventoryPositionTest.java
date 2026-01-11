package minestats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Position;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;
import fr.emalios.mystats.impl.storage.dao.InventoryPositionsDao;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryPositionsRepository;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

@DisplayName("InventoriesPositions test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqliteInventoryPositionTest {

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        var playerInvRepo = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        Storage.registerPlayerInventoriesRepo(playerInvRepo);
        Storage.registerPlayerRepo(new SqlitePlayerRepository(new PlayerDao(conn), playerInvRepo));
        Storage.registerInventoryRepo(new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn)));
        Storage.registerInventoryPositionRepo(new SqliteInventoryPositionsRepository(new InventoryPositionsDao(conn)));
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Add position to inventory")
    public void addPositionToInventory() {
        Position p1 = new Position("add-position", 0, 0, 0);
        Position p2 = new Position("add-position", 0, 1, 0);
        Inventory inventory = Storage.inventories().getOrCreate(p1);

        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p1));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p2));

        Storage.inventoryPositions().addPosition(inventory, p2);
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p2));
    }

    @Test
    @DisplayName("Add position to non persisted inventory")
    public void addPositionToNonPersistedInventory() {
        Position p1 = new Position("add-position-non-persisted", 0, 0, 0);
        Inventory inventory = new Inventory(Set.of(p1));
        Assertions.assertFalse(inventory.isPersisted());

        Assertions.assertThrows(IllegalStateException.class, () -> Storage.inventoryPositions().addPosition(inventory, p1));
    }

    @Test
    @DisplayName("Add duplicated position to inventory")
    public void addDuplicatePositionToInventory() {
        Position p1 = new Position("add-position-duplicated", 0, 0, 0);
        Position p2 = new Position("add-position-duplicated", 0, 0, 0);
        Assertions.assertEquals(p1, p2);

        Inventory inventory = Storage.inventories().getOrCreate(p1);

        var e = Assertions.assertThrows(RuntimeException.class, () -> Storage.inventoryPositions().addPosition(inventory, p2));
        Assertions.assertInstanceOf(SQLiteException.class, e.getCause());
    }

    @Test
    @DisplayName("Get positions of an inventory from inventory")
    public void getPositionsOfInventoryByInventory() {
        Position p1 = new Position("get-position-from-inv", 0, 0, 0);
        Position p2 = new Position("get-position-from-inv", 0, 1, 0);
        Position p3 = new Position("get-position-from-inv", 0, 0, 1);
        Position p4 = new Position("get-position-from-inv", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        var results = Storage.inventoryPositions().findAllByInventory(inventory);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(inventory.getInvPositions(), results);
    }

    @Test
    @DisplayName("Get positions of an inventory from non persisted inventory")
    public void getPositionsOfNonPersistedInventoryByInventory() {
        Position p1 = new Position("get-position-from-non-persisted-inv", 0, 0, 0);
        Position p2 = new Position("get-position-from-non-persisted-inv", 0, 1, 0);
        Position p3 = new Position("get-position-from-non-persisted-inv", 0, 0, 1);
        Position p4 = new Position("get-position-from-non-persisted-inv", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);

        Assertions.assertThrows(IllegalStateException.class, () -> Storage.inventoryPositions().findAllByInventory(inventory));
    }

    @Test
    @DisplayName("Get positions of an inventory from inventory id")
    public void getPositionsOfInventoryByInventoryId() {
        Position p1 = new Position("get-position-from-inv-id", 0, 0, 0);
        Position p2 = new Position("get-position-from-inv-id", 0, 1, 0);
        Position p3 = new Position("get-position-from-inv-id", 0, 0, 1);
        Position p4 = new Position("get-position-from-inv-id", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        var results = Storage.inventoryPositions().findAllByInventoryId(inventory.getId());
        Assertions.assertNotNull(results);
        Assertions.assertEquals(inventory.getInvPositions(), results);
    }

    @Test
    @DisplayName("Get positions of an inventory from non existing inventory id")
    public void getPositionsOfNonExistingInventoryByInventoryId() {
        Position p1 = new Position("get-position-from-null-inv-id", 0, 0, 0);
        Position p2 = new Position("get-position-from-null-inv-id", 0, 1, 0);
        Set<Position> positions = Set.of(p1, p2);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        var results = Storage.inventoryPositions().findAllByInventoryId(-1);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Get position of an unstored inventory")
    public void getPositionOfUnstoredInventory() {
        Position p1 = new Position("get-position-unstored", 0, 0, 0);
        Position p2 = new Position("get-position-unstored", 0, 1, 0);
        Position p3 = new Position("get-position-unstored", 0, 0, 1);
        Position p4 = new Position("get-position-unstored", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);

        //from each position we should get the inventory with all positions
        for (Position position : positions) {
            Optional<Inventory> result = Storage.inventories().findByPos(position);
            Assertions.assertTrue(result.isEmpty());
        }
    }

    @Test
    @DisplayName("Delete position of an inventory")
    public void deletePositionOfInventory() {
        Position p1 = new Position("delete-position", 0, 0, 0);
        Position p2 = new Position("delete-position", 0, 1, 0);
        Position p3 = new Position("delete-position", 0, 0, 1);
        Position p4 = new Position("delete-position", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        Assertions.assertTrue(Storage.inventoryPositions().removePosition(inventory, p1));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p1));
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p2));
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p3));
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p4));

        Assertions.assertTrue(Storage.inventoryPositions().removePosition(inventory, p2));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p1));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p2));
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p3));
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p4));

        Assertions.assertTrue(Storage.inventoryPositions().removePosition(inventory, p3));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p1));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p2));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p3));
        Assertions.assertTrue(Storage.inventoryPositions().hasPosition(inventory, p4));

        Assertions.assertTrue(Storage.inventoryPositions().removePosition(inventory, p4));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p1));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p2));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p3));
        Assertions.assertFalse(Storage.inventoryPositions().hasPosition(inventory, p4));
    }

    @Test
    @DisplayName("Delete position of an unstored inventory")
    public void deletePositionOfUnstoredInventory() {
        Position p1 = new Position("delete-position-unstored", 0, 0, 0);
        Position p2 = new Position("delete-position-unstored", 0, 1, 0);
        Position p3 = new Position("delete-position-unstored", 0, 0, 1);
        Position p4 = new Position("delete-position-unstored", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);

        Assertions.assertThrows(IllegalStateException.class, () -> Storage.inventoryPositions().removePosition(inventory, p1));
        Assertions.assertThrows(IllegalStateException.class, () -> Storage.inventoryPositions().removePosition(inventory, p2));
        Assertions.assertThrows(IllegalStateException.class, () -> Storage.inventoryPositions().removePosition(inventory, p3));
        Assertions.assertThrows(IllegalStateException.class, () -> Storage.inventoryPositions().removePosition(inventory, p4));
    }

    @Test
    @DisplayName("Delete non existing position of an inventory")
    public void deleteNonExistingPositionOfInventory() {
        Position p1 = new Position("delete-position-null", 0, 0, 0);
        Position p2 = new Position("delete-position-null", 0, 1, 0);
        Position p3 = new Position("delete-position-null", 0, 0, 1);
        Position p4 = new Position("delete-position-null", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        Assertions.assertFalse(Storage.inventoryPositions().removePosition(inventory, p4));
    }

}
