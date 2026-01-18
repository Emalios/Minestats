package minestats.api.storage;

import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.storage.*;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.*;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

@DisplayName("InventoriesPositions test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqliteInventoryPositionTest {

    private PlayerInventoryRepository playerInventoryRepository;
    private PlayerRepository playerRepository;
    private InventoryRepository inventoryRepository;
    private InventorySnapshotRepository inventorySnapshotRepository;
    private InventoryPositionsRepository inventoryPositionsRepository;

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        this.playerInventoryRepository = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        this.playerRepository = new SqlitePlayerRepository(new PlayerDao(conn), this.playerInventoryRepository);
        this.inventoryRepository = new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn));
        this.inventorySnapshotRepository = new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn));
        this.inventoryPositionsRepository = new SqliteInventoryPositionsRepository(new InventoryPositionsDao(conn));
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
        Inventory inventory = this.inventoryRepository.getOrCreate(p1);

        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p1));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p2));

        this.inventoryPositionsRepository.addPosition(inventory, p2);
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p2));
    }

    @Test
    @DisplayName("Add position to non persisted inventory")
    public void addPositionToNonPersistedInventory() {
        Position p1 = new Position("add-position-non-persisted", 0, 0, 0);
        Inventory inventory = new Inventory(Set.of(p1));
        Assertions.assertFalse(inventory.isPersisted());

        Assertions.assertThrows(IllegalStateException.class, () -> this.inventoryPositionsRepository.addPosition(inventory, p1));
    }

    @Test
    @DisplayName("Add duplicated position to inventory")
    public void addDuplicatePositionToInventory() {
        Position p1 = new Position("add-position-duplicated", 0, 0, 0);
        Position p2 = new Position("add-position-duplicated", 0, 0, 0);
        Assertions.assertEquals(p1, p2);

        Inventory inventory = this.inventoryRepository.getOrCreate(p1);

        var e = Assertions.assertThrows(RuntimeException.class, () -> this.inventoryPositionsRepository.addPosition(inventory, p2));
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
        this.inventoryRepository.save(inventory);

        var results = this.inventoryPositionsRepository.findAllByInventory(inventory);
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

        Assertions.assertThrows(IllegalStateException.class, () -> this.inventoryPositionsRepository.findAllByInventory(inventory));
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
        this.inventoryRepository.save(inventory);

        var results = this.inventoryPositionsRepository.findAllByInventoryId(inventory.getId());
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
        this.inventoryRepository.save(inventory);

        var results = this.inventoryPositionsRepository.findAllByInventoryId(-1);
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
            Optional<Inventory> result = this.inventoryRepository.findByPos(position);
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
        this.inventoryRepository.save(inventory);

        Assertions.assertTrue(this.inventoryPositionsRepository.removePosition(inventory, p1));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p1));
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p2));
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p3));
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p4));

        Assertions.assertTrue(this.inventoryPositionsRepository.removePosition(inventory, p2));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p1));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p2));
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p3));
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p4));

        Assertions.assertTrue(this.inventoryPositionsRepository.removePosition(inventory, p3));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p1));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p2));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p3));
        Assertions.assertTrue(this.inventoryPositionsRepository.hasPosition(inventory, p4));

        Assertions.assertTrue(this.inventoryPositionsRepository.removePosition(inventory, p4));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p1));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p2));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p3));
        Assertions.assertFalse(this.inventoryPositionsRepository.hasPosition(inventory, p4));
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

        Assertions.assertThrows(IllegalStateException.class, () -> this.inventoryPositionsRepository.removePosition(inventory, p1));
        Assertions.assertThrows(IllegalStateException.class, () -> this.inventoryPositionsRepository.removePosition(inventory, p2));
        Assertions.assertThrows(IllegalStateException.class, () -> this.inventoryPositionsRepository.removePosition(inventory, p3));
        Assertions.assertThrows(IllegalStateException.class, () -> this.inventoryPositionsRepository.removePosition(inventory, p4));
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
        this.inventoryRepository.save(inventory);

        Assertions.assertFalse(this.inventoryPositionsRepository.removePosition(inventory, p4));
    }

}
