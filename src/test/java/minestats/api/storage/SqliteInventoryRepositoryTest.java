package minestats.api.storage;

import fr.emalios.mystats.api.*;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.*;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@DisplayName("InventoryRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SqliteInventoryRepositoryTest {

    private InventoryRepository repository;

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        var playerInvRepo = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        Storage.registerInventorySnapshotRepo(new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn)));
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
    @Order(6)
    @DisplayName("Create inventory")
    void addInventoryTest() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("create-inv", 0, 1, 2));

        Assertions.assertTrue(inventory.isPersisted());
        Assertions.assertNotNull(inventory.getId());
        Set<Position> positions = inventory.getInvPositions();
        Assertions.assertNotNull(positions);
        Assertions.assertFalse(positions.isEmpty());
        Assertions.assertEquals(1, positions.size());
        Assertions.assertTrue(positions.contains(new Position("create-inv", 0, 1, 2)));
    }

    @Test
    @Order(5)
    @DisplayName("Create multiple inventories")
    void addMultipleInventoryTest() {
        Inventory inv1 = Storage.inventories().getOrCreate(new Position("multiple-inv-1", 0, 1, 2));

        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertNotNull(inv1.getId());
        Set<Position> pos1 = inv1.getInvPositions();
        Assertions.assertNotNull(pos1);
        Assertions.assertFalse(pos1.isEmpty());
        Assertions.assertEquals(1, pos1.size());
        Assertions.assertTrue(pos1.contains(new Position("multiple-inv-1", 0, 1, 2)));

        Inventory inv2 = Storage.inventories().getOrCreate(new Position("multiple-inv-2", 0, 10, 20));

        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertNotNull(inv2.getId());
        Set<Position> pos2 = inv2.getInvPositions();
        Assertions.assertNotNull(pos2);
        Assertions.assertFalse(pos2.isEmpty());
        Assertions.assertEquals(1, pos2.size());
        Assertions.assertTrue(pos2.contains(new Position("multiple-inv-2", 0, 10, 20)));

        Inventory inv3 = Storage.inventories().getOrCreate(new Position("multiple-inv-3", 0, 100, 200));

        Assertions.assertTrue(inv3.isPersisted());
        Assertions.assertNotNull(inv3.getId());
        Set<Position> pos3 = inv3.getInvPositions();
        Assertions.assertNotNull(pos3);
        Assertions.assertFalse(pos3.isEmpty());
        Assertions.assertEquals(1, pos3.size());
        Assertions.assertTrue(pos3.contains(new Position("multiple-inv-3", 0, 100, 200)));
    }

    @Test
    @Order(4)
    @DisplayName("Create inventory with multiple positions")
    void createInventoryWithMultiplePositionsTest() {
        Set<Position> positions = new HashSet<>();
        Position p1 = new Position("multiple-inv-position", 0, 1, 2);
        Position p2 = new Position("multiple-inv-position", 0, 10, 20);
        Position p3 = new Position("multiple-inv-position", 0, 100, 200);
        positions.add(p1);
        positions.add(p2);
        positions.add(p3);
        Inventory inv1 = new Inventory(positions);
        Storage.inventories().save(inv1);

        Inventory storedInv = Storage.inventories().getOrCreate(p1);
        Set<Position> storedInvPositions = storedInv.getInvPositions();
        Assertions.assertNotNull(storedInvPositions);
        Assertions.assertFalse(storedInvPositions.isEmpty());
        Assertions.assertEquals(3, storedInvPositions.size());
        Assertions.assertEquals(Storage.inventories().getOrCreate(p2), storedInv);
        Assertions.assertEquals(Storage.inventories().getOrCreate(p3), storedInv);
    }

    @Test
    @Order(3)
    @DisplayName("Get existing inventory")
    void getExistingInventoryTest() {
        Inventory inv1 = new Inventory(Set.of(new Position("get-existing-inv", 3, 4, 5)));
        Storage.inventories().save(inv1);
        //Inventory inv1 = Storage.inventories().getOrCreate(new Position("get-existing-inv", 3, 4, 5));
        Optional<Inventory> optInv2 = Storage.inventories().findByPos(new Position("get-existing-inv", 3, 4, 5));
        Assertions.assertTrue(optInv2.isPresent());
        Inventory inv2 = optInv2.get();
        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertEquals(inv1.getId(), inv2.getId());
        Assertions.assertEquals(inv1, inv2);
    }

    @Test
    @Order(2)
    @DisplayName("Get non existing inventory")
    void getNonExistingInventoryTest() {
        Inventory inv1 = Storage.inventories().getOrCreate(new Position("get-non-existing-inv", 3, 4, 5));
        Inventory inv2 = Storage.inventories().getOrCreate(new Position("get-non-existing-inv", 3, 4, 5));
        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertEquals(inv1.getId(), inv2.getId());
        Assertions.assertEquals(inv1, inv2);
    }

    @Test
    @Order(1)
    @DisplayName("Get all inventories")
    void getAllInventoryTest() {
        Position p1 = new Position("get-all-inv-1", 3, 4, 5);
        Position p2 = new Position("get-all-inv-2", 3, 4, 5);
        Position p3 = new Position("get-all-inv-3", 3, 4, 5);
        Inventory inv1 = new Inventory(Set.of(p1));
        Storage.inventories().save(inv1);
        Inventory inv2 = new Inventory(Set.of(p2));
        Storage.inventories().save(inv2);
        Inventory inv3 = new Inventory(Set.of(p3));
        Storage.inventories().save(inv3);
        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertTrue(inv3.isPersisted());

        Collection<Inventory> inventories = Storage.inventories().getAll();
        Assertions.assertNotNull(inventories);
        Assertions.assertFalse(inventories.isEmpty());
        Assertions.assertEquals(3, inventories.size());
        Assertions.assertTrue(inventories.contains(inv1));
        Assertions.assertTrue(inventories.contains(inv2));
        Assertions.assertTrue(inventories.contains(inv3));
    }

    @Test
    @Order(7)
    @DisplayName("Get all inventories from world")
    void getAllInventoryFromWorldTest() {
        String world = "get-all-inv-from-world";
        Position p1 = new Position(world, 3, 4, 5);
        Position p2 = new Position(world, 30, 4, 5);
        Position p3 = new Position(world, 3, 40, 5);
        Inventory inv1 = new Inventory(Set.of(p1));
        Storage.inventories().save(inv1);
        Inventory inv2 = new Inventory(Set.of(p2));
        Storage.inventories().save(inv2);
        Inventory inv3 = new Inventory(Set.of(p3));
        Storage.inventories().save(inv3);
        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertTrue(inv3.isPersisted());

        Collection<Inventory> inventories = Storage.inventories().getAllFromWorld(world);
        Assertions.assertNotNull(inventories);
        Assertions.assertFalse(inventories.isEmpty());
        Assertions.assertEquals(3, inventories.size());
        Assertions.assertTrue(inventories.contains(inv1));
        Assertions.assertTrue(inventories.contains(inv2));
        Assertions.assertTrue(inventories.contains(inv3));
    }

    @Test
    @DisplayName("Get inventory from one of its position")
    public void getPositionsOfInventoryByOnePosition() {
        Position p1 = new Position("get-position", 0, 0, 0);
        Position p2 = new Position("get-position", 0, 1, 0);
        Position p3 = new Position("get-position", 0, 0, 1);
        Position p4 = new Position("get-position", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        //from each position we should get the inventory with all positions
        for (Position position : positions) {
            Inventory result = Storage.inventories().getOrCreate(position);
            Assertions.assertNotNull(result);
            Assertions.assertEquals(inventory.getInvPositions(), result.getInvPositions());
            Assertions.assertEquals(inventory, result);
        }
    }

    @Test
    @DisplayName("Find inventory from one of its position")
    public void findPositionsOfInventoryByOnePosition() {
        Position p1 = new Position("find-position", 0, 0, 0);
        Position p2 = new Position("find-position", 0, 1, 0);
        Position p3 = new Position("find-position", 0, 0, 1);
        Position p4 = new Position("find-position", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);

        //from each position we should get the inventory with all positions
        for (Position position : positions) {
            Optional<Inventory> result = Storage.inventories().findByPos(position);
            Assertions.assertNotNull(result);
            Assertions.assertTrue(result.isPresent());
            Assertions.assertEquals(inventory.getInvPositions(), result.get().getInvPositions());
            Assertions.assertEquals(inventory, result.get());
        }
    }

    @Test
    @DisplayName("Get inventory from id")
    public void getInventoryFromIdTest() {
        Position p1 = new Position("get-position-from-id", 0, 0, 0);
        Position p2 = new Position("get-position-from-id", 0, 1, 0);
        Position p3 = new Position("get-position-from-id", 0, 0, 1);
        Position p4 = new Position("get-position-from-id", 0, 1, 1);
        Set<Position> positions = Set.of(p1, p2, p3, p4);
        Inventory inventory = new Inventory(positions);
        Storage.inventories().save(inventory);
        int invId = inventory.getId();

        Inventory result = Storage.inventories().getById(invId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(inventory.getInvPositions(), result.getInvPositions());
        Assertions.assertEquals(inventory, result);
    }

    @Test
    @Order(8)
    @DisplayName("Delete existing inventory")
    void deleteExistingInventoryTest() {
        Inventory inv = Storage.inventories().getOrCreate(new Position("delete-existing", 0, 0, 0));
        Storage.inventories().delete(inv);
        Optional<Inventory> optInv =  Storage.inventories().findByPos(new Position("delete-existing", 0, 0, 0));
        Assertions.assertFalse(optInv.isPresent());
    }

    @Test
    @Order(9)
    @DisplayName("Delete existing inventory with many positions")
    void deleteExistingInventoryWithManyPositionsTest() {
        Set<Position> positions = new HashSet<>();
        Position p1 = new Position("delete-multiple-inv-position", 0, 1, 2);
        Position p2 = new Position("delete-multiple-inv-position", 0, 10, 20);
        Position p3 = new Position("delete-multiple-inv-position", 0, 100, 200);
        positions.add(p1);
        positions.add(p2);
        positions.add(p3);
        Inventory inv1 = new Inventory(positions);
        Storage.inventories().save(inv1);
        int invId = inv1.getId();

        Storage.inventories().delete(inv1);
        Assertions.assertThrows(IllegalArgumentException.class, () -> Storage.inventories().getById(invId));
        Assertions.assertTrue(Storage.inventories().findByPos(p1).isEmpty());
        Assertions.assertTrue(Storage.inventories().findByPos(p2).isEmpty());
        Assertions.assertTrue(Storage.inventories().findByPos(p3).isEmpty());
        Assertions.assertTrue(Storage.inventoryPositions().findAllByInventoryId(invId).isEmpty());
    }

    @Test
    @Order(10)
    @DisplayName("Delete existing inventory with snapshots")
    void deleteExistingSnapshotTest() {
        Inventory inv = Storage.inventories().getOrCreate(new Position("test-delete-inv-snapshot", 0, 0, 0));
        inv.addHandler(new IHandler() {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public Collection<Record> getContent() {
                return List.of(new Record(RecordType.ITEM, "test", 0, CountUnit.ITEM));
            }
        });
        inv.recordContent();
        inv.recordContent();

        int invId = inv.getId();
        Storage.inventories().delete(inv);
        Optional<Inventory> optInv = Storage.inventories().findByPos(new Position("test-delete-inv-snapshot-2", 0, 0, 0));

        Assertions.assertFalse(optInv.isPresent());
        Assertions.assertFalse(inv.isPersisted());

        var snapshots = Storage.inventorySnapshots().findAllByInventoryId(invId);
        Assertions.assertEquals(0, snapshots.size());
    }

    @Test
    @Order(11)
    @DisplayName("Delete existing inventory associated with player")
    void deleteExistingInventoryAssociatedWithPlayerTest() {
        StatPlayer player = Storage.players().getOrCreate("test-delete-inv-player");
        Inventory inv = Storage.inventories().getOrCreate(new Position("test-delete-inv-player-associated", 0, 0, 0));
        inv.addHandler(new IHandler() {
            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public Collection<Record> getContent() {
                return List.of(new Record(RecordType.ITEM, "test", 0, CountUnit.ITEM));
            }
        });
        player.addInventory(inv);
        inv.recordContent();
        inv.recordContent();

        Storage.inventories().delete(inv);
        Optional<Inventory> optInv = Storage.inventories().findByPos(new Position("test-delete-inv-player-associated", 0, 0, 0));

        Assertions.assertFalse(optInv.isPresent());
        Assertions.assertFalse(inv.isPersisted());

        var result = Storage.playerInventories().findByPlayer(player);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @Order(12)
    @DisplayName("Delete non existing inventory")
    void deleteNonExistingInventoryTest() {
        Inventory inventory = new Inventory(Set.of(new Position("delete-non-existing-inv", 0, 0, 0)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Storage.inventories().delete(inventory));
    }

}
