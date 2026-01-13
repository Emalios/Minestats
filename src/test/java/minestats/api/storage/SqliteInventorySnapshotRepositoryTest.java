package minestats.api.storage;

import fr.emalios.mystats.api.*;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqliteInventorySnapshotRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("InventorySnapshotRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqliteInventorySnapshotRepositoryTest {

    private List<Record> itemRecords = List.of(
            new Record(RecordType.ITEM, "dirt", 10, CountUnit.ITEM),
            new Record(RecordType.ITEM, "stone", 100, CountUnit.ITEM),
            new Record(RecordType.ITEM, "iron", 1000, CountUnit.ITEM)
    );
    private List<Record> fluidRecords = List.of(
            new Record(RecordType.FLUID, "water", 1, CountUnit.B),
            new Record(RecordType.FLUID, "lava", 100, CountUnit.MB)
    );

    private IHandler items = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return itemRecords;
        }
    };

    private IHandler fluids = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return fluidRecords;
        }
    };

    private IHandler empty = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return List.of();
        }
    };

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        var playerInvRepo = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        Storage.registerInventorySnapshotRepo(new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn)));
        Storage.registerPlayerInventoriesRepo(playerInvRepo);
        Storage.registerPlayerRepo(new SqlitePlayerRepository(new PlayerDao(conn), playerInvRepo));
        Storage.registerInventoryRepo(new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn)));
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Create multiple empty snapshots")
    void createMultipleEmptySnapshots() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:overworld", 0, 0, 0));
        for (int i = 0; i < 10; i++) {
            inventory.recordContent();
        }
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Assertions.assertNotNull(invSnapshots);
        Assertions.assertFalse(invSnapshots.isEmpty());
        Assertions.assertEquals(10, invSnapshots.size());
    }

    @Test
    @DisplayName("Create item snapshot")
    void createItemSnapshot() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:nether", 0, 0, 0));
        inventory.addHandler(items);
        inventory.recordContent();
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Snapshot snapshot = invSnapshots.getFirst();
        var content = snapshot.getContent();
        Assertions.assertNotNull(content);
        Assertions.assertFalse(content.isEmpty());
        Assertions.assertEquals(itemRecords.size(), content.size());
        Assertions.assertTrue(
                content.containsAll(itemRecords) &&
                        itemRecords.containsAll(content)
        );
    }

    @Test
    @DisplayName("Create fluid snapshot")
    void createFluidSnapshot() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:end", 0, 0, 0));
        inventory.addHandler(fluids);
        inventory.recordContent();
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Snapshot snapshot = invSnapshots.getFirst();
        var content = snapshot.getContent();
        Assertions.assertNotNull(content);
        Assertions.assertFalse(content.isEmpty());
        Assertions.assertEquals(fluidRecords.size(), content.size());
        Assertions.assertTrue(
                content.containsAll(fluidRecords) &&
                        fluidRecords.containsAll(content)
        );
    }

    @Test
    @DisplayName("Create empty snapshot")
    void createEmptySnapshot() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:overworld", 10, 0, 0));
        inventory.addHandler(empty);
        inventory.recordContent();
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Snapshot snapshot = invSnapshots.getFirst();
        var content = snapshot.getContent();
        Assertions.assertNotNull(content);
        Assertions.assertTrue(content.isEmpty());
        Assertions.assertEquals(List.of(), content);
    }

    @Test
    @DisplayName("Create mixed snapshot")
    void createMixedSnapshot() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:overworld", 0, 10, 0));
        inventory.addHandler(fluids);
        inventory.addHandler(items);
        inventory.recordContent();
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Snapshot snapshot = invSnapshots.getFirst();
        var content = snapshot.getContent();
        Assertions.assertNotNull(content);
        Assertions.assertFalse(content.isEmpty());
        Assertions.assertTrue(
                content.containsAll(fluidRecords) &&
                        content.containsAll(itemRecords)
        );
    }

    @Test
    @DisplayName("Create mixed snapshots")
    void createMixedSnapshots() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:overworld", 0, 0, 10));
        inventory.addHandler(fluids);
        inventory.addHandler(items);
        inventory.recordContent();
        inventory.recordContent();
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Assertions.assertEquals(2, invSnapshots.size());
    }

    /**
     * We do not support that yet.
     */
    //@Test
    @DisplayName("Create snapshot with merged records")
    void createSnapshotWithMergedRecords() {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("minecraft:overworld", 10, 10, 10));
        inventory.addHandler(items);
        inventory.addHandler(items);
        inventory.recordContent();
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Snapshot snapshot = invSnapshots.getFirst();
        Assertions.assertEquals(3, snapshot.getContent().size());
        Assertions.assertEquals(List.of(
                new Record(RecordType.ITEM, "dirt", 10*2, CountUnit.ITEM),
                new Record(RecordType.ITEM, "stone", 100*2, CountUnit.ITEM),
                new Record(RecordType.ITEM, "iron", 1000*2, CountUnit.ITEM)
        ), snapshot.getContent());
    }

    @Test
    @DisplayName("Get all snapshots")
    void getAllSnapshots() throws InterruptedException {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("all-snapshot-test", 0, 0, 10));
        inventory.addHandler(items);
        int numberOfSnapshots = 15;
        for (int i = 0; i < numberOfSnapshots; i++) {
            inventory.recordContent();
            TimeUnit.SECONDS.sleep(1);
        }
        var invSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        Assertions.assertEquals(numberOfSnapshots, invSnapshots.size());
        assertOrder(invSnapshots);
    }

    @Test
    @DisplayName("Get 10 last snapshots")
    void get10LastSnapshots() throws InterruptedException {
        Inventory inventory = Storage.inventories().getOrCreate(new Position("10-snapshot-test", 0, 0, 10));
        inventory.addHandler(items);
        int numberOfSnapshots = 15;
        int wantedNumberOfSnapshots = 10;
        Assertions.assertTrue(numberOfSnapshots > wantedNumberOfSnapshots);
        for (int i = 0; i < numberOfSnapshots; i++) {
            inventory.recordContent();
            TimeUnit.SECONDS.sleep(1);
        }
        var invSnapshots = Storage.inventorySnapshots().findLastByInventory(inventory, wantedNumberOfSnapshots);
        assertOrder(invSnapshots);
        Assertions.assertEquals(wantedNumberOfSnapshots, invSnapshots.size());
        //assert the got snapshots are the last of the db
        var totalSnapshots = Storage.inventorySnapshots().findAllByInventory(inventory);
        int startingIndex = numberOfSnapshots - wantedNumberOfSnapshots;
        for (int i = startingIndex; i < numberOfSnapshots; i++) {
            Snapshot s1 = totalSnapshots.get(i);
            Snapshot s2 = invSnapshots.get(i - startingIndex);
            Assertions.assertEquals(s1, s2);
        }
    }

    private void assertOrder(List<Snapshot> snapshots) {
        long previous = Long.MIN_VALUE;
        for (var snapshot : snapshots) {
            long current = snapshot.getTimestamp();
            Assertions.assertTrue(
                    current >= previous,
                    "Snapshots non ordonnés : " + previous + " > " + current
            );
            previous = current;
        }
    }


}
