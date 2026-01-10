package minestats.api.storage;

import fr.emalios.mystats.api.*;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.adapter.ItemAdapter;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqliteInventorySnapshotRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@DisplayName("InventoryRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqliteInventoryRepositoryTest {

    private InventoryRepository repository;

    @BeforeAll
    void setup() {
        Connection conn = DatabaseTest.getConnection();

        Storage.registerInventorySnapshotRepo(new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn)));
        Storage.registerPlayerInventoriesRepo(new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn)));
        Storage.registerPlayerRepo(new SqlitePlayerRepository(new PlayerDao(conn)));
        Storage.registerInventoryRepo(new SqliteInventoryRepository(new InventoryDao(conn)));
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Create inventory")
    void addInventoryTest() {
        Inventory inventory = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);

        Assertions.assertTrue(inventory.isPersisted());
        Assertions.assertNotNull(inventory.getId());
        Assertions.assertEquals("minecraft:overworld", inventory.getWorld());
        Assertions.assertEquals(0, inventory.getX());
        Assertions.assertEquals(1, inventory.getY());
        Assertions.assertEquals(2, inventory.getZ());
    }

    @Test
    @DisplayName("Create multiple inventories")
    void addMultipleInventoryTest() {
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);

        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertNotNull(inv1.getId());
        Assertions.assertEquals("minecraft:overworld", inv1.getWorld());
        Assertions.assertEquals(0, inv1.getX());
        Assertions.assertEquals(1, inv1.getY());
        Assertions.assertEquals(2, inv1.getZ());

        Inventory inv2 = Storage.inventories().getOrCreate("minecraft:nether", 0, 10, 20);

        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertNotNull(inv2.getId());
        Assertions.assertEquals("minecraft:nether", inv2.getWorld());
        Assertions.assertEquals(0, inv2.getX());
        Assertions.assertEquals(10, inv2.getY());
        Assertions.assertEquals(20, inv2.getZ());

        Inventory inv3 = Storage.inventories().getOrCreate("minecraft:end", 0, 100, 200);

        Assertions.assertTrue(inv3.isPersisted());
        Assertions.assertNotNull(inv3.getId());
        Assertions.assertEquals("minecraft:end", inv3.getWorld());
        Assertions.assertEquals(0, inv3.getX());
        Assertions.assertEquals(100, inv3.getY());
        Assertions.assertEquals(200, inv3.getZ());
    }

    @Test
    @DisplayName("Get existing inventory")
    void getExistingInventoryTest() {
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 3, 4, 5);
        Optional<Inventory> optInv2 = Storage.inventories().findByPos("minecraft:overworld", 3, 4, 5);
        Assertions.assertTrue(optInv2.isPresent());
        Inventory inv2 = optInv2.get();
        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertEquals(inv1.getId(), inv2.getId());
        Assertions.assertEquals(inv1, inv2);
    }

    @Test
    @DisplayName("Get non existing inventory")
    void getNonExistingInventoryTest() {
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 3, 4, 5);
        Inventory inv2 = Storage.inventories().getOrCreate("minecraft:overworld", 3, 4, 5);
        Assertions.assertTrue(inv1.isPersisted());
        Assertions.assertTrue(inv2.isPersisted());
        Assertions.assertEquals(inv1.getId(), inv2.getId());
        Assertions.assertEquals(inv1, inv2);
    }

    @Test
    @DisplayName("Delete existing inventory")
    void deleteExistingInventoryTest() {
        Inventory inv = Storage.inventories().getOrCreate("minecraft:nether", 0, 0, 0);
        Storage.inventories().delete(inv);
        Optional<Inventory> optInv =  Storage.inventories().findByPos("minecraft:nether", 0, 0, 0);
        Assertions.assertFalse(optInv.isPresent());
    }

    @Test
    @DisplayName("Delete existing inventory with snapshots")
    void deleteExistingSnapshotTest() {
        Inventory inv = Storage.inventories().getOrCreate("test-delete-inv-snapshot", 0, 0, 0);
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
        Optional<Inventory> optInv = Storage.inventories().findByPos("minecraft:nether", 0, 0, 0);

        Assertions.assertFalse(optInv.isPresent());
        Assertions.assertFalse(inv.isPersisted());

        var snapshots = Storage.inventorySnapshots().findAllByInventoryId(invId);
        Assertions.assertEquals(0, snapshots.size());
    }

    @Test
    @DisplayName("Delete existing inventory associated with player")
    void deleteExistingInventoryAssociatedWithPlayerTest() {
        StatPlayer player = Storage.players().getOrCreate("test-delete-inv-player");
        Inventory inv = Storage.inventories().getOrCreate("test-delete-inv-player-associated", 0, 0, 0);
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
        Optional<Inventory> optInv = Storage.inventories().findByPos("minecraft:nether", 0, 0, 0);

        Assertions.assertFalse(optInv.isPresent());
        Assertions.assertFalse(inv.isPersisted());

        var result = Storage.playerInventories().findByPlayer(player);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Delete non existing inventory")
    void deleteNonExistingInventoryTest() {
        Inventory inventory = new Inventory("minecraft:end", 0, 0, 0);
        Assertions.assertThrows(IllegalArgumentException.class, () -> Storage.inventories().delete(inventory));
    }

}
