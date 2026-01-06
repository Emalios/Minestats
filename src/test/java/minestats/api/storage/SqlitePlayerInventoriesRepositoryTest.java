package minestats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;

@DisplayName("PlayerInventoriesRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqlitePlayerInventoriesRepositoryTest {


    @BeforeAll
    void setup() {
        Connection conn = DatabaseTest.getConnection();
        Storage.registerPlayerInventoriesRepo(new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn)));
        Storage.registerPlayerRepo(new SqlitePlayerRepository(new PlayerDao(conn)));
        Storage.registerInventoryRepo(new SqliteInventoryRepository(new InventoryDao(conn)));
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Assign inventory")
    void assignInventoryTest() {
        StatPlayer statPlayer = Storage.players().getOrCreate("player1");
        Inventory inventory = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);

        statPlayer.addInventory(inventory);

        Assertions.assertTrue(statPlayer.hasInventory(inventory));

        StatPlayer updatedPlayer = Storage.players().getOrCreate("player1");
        Inventory updatedInventory = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);

        Assertions.assertTrue(updatedPlayer.hasInventory(updatedInventory));
    }

    @Test
    @DisplayName("Assign multiple inventories")
    void assignMultipleInventoriesTest() {
        StatPlayer statPlayer = Storage.players().getOrCreate("player1");
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);
        Inventory inv2 = Storage.inventories().getOrCreate("minecraft:nether", 6, 17, 22);
        Inventory inv3 = Storage.inventories().getOrCreate("minecraft:end", 96, 54, 29);

        statPlayer.addInventory(inv1);
        statPlayer.addInventory(inv2);
        statPlayer.addInventory(inv3);

        Assertions.assertTrue(statPlayer.hasInventory(inv1));
        Assertions.assertTrue(statPlayer.hasInventory(inv2));
        Assertions.assertTrue(statPlayer.hasInventory(inv3));
    }

    @Test
    @DisplayName("Assign non persisted inventory")
    void assignNonPersistedInventoryTest() {
        StatPlayer statPlayer = Storage.players().getOrCreate("player1");
        Inventory inventory = new Inventory("minecraft:overworld", 0, 1, 2);

        Assertions.assertThrows(IllegalStateException.class, () -> statPlayer.addInventory(inventory));
    }

    @Test
    @DisplayName("Assign non persisted player")
    void assignNonPersistedPlayerTest() {
        StatPlayer statPlayer = new StatPlayer("player1");
        Inventory inventory = new Inventory("minecraft:overworld", 0, 1, 2);

        Assertions.assertThrows(IllegalStateException.class, () -> statPlayer.addInventory(inventory));
    }

    @Test
    @DisplayName("Remove assigned inventory")
    void removeInventoryTest() {
        StatPlayer statPlayer = Storage.players().getOrCreate("player1");
        Inventory inventory = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);

        statPlayer.addInventory(inventory);
        Assertions.assertTrue(statPlayer.hasInventory(inventory));

        boolean deleted = statPlayer.removeInventory(inventory);
        Assertions.assertTrue(deleted);
        Assertions.assertFalse(statPlayer.hasInventory(inventory));
    }

    @Test
    @DisplayName("Remove unassigned inventory")
    void removeUnassignedInventoryTest() {
        StatPlayer statPlayer = Storage.players().getOrCreate("player1");
        Inventory inventory = Storage.inventories().getOrCreate("minecraft:nether", 0, 1, 2);

        boolean deleted = statPlayer.removeInventory(inventory);
        Assertions.assertFalse(deleted);
    }

    @Test
    @DisplayName("Remove multiple inventories")
    void removeMultipleInventoriesTest() {
        StatPlayer statPlayer = Storage.players().getOrCreate("player1");
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 0, 1, 2);
        Inventory inv2 = Storage.inventories().getOrCreate("minecraft:nether", 6, 17, 22);
        Inventory inv3 = Storage.inventories().getOrCreate("minecraft:end", 96, 54, 29);

        statPlayer.addInventory(inv1);
        statPlayer.addInventory(inv2);
        statPlayer.addInventory(inv3);

        Assertions.assertTrue(statPlayer.removeInventory(inv1));
        Assertions.assertTrue(statPlayer.removeInventory(inv2));
        Assertions.assertTrue(statPlayer.removeInventory(inv3));

        Assertions.assertFalse(statPlayer.hasInventory(inv1));
        Assertions.assertFalse(statPlayer.hasInventory(inv2));
        Assertions.assertFalse(statPlayer.hasInventory(inv3));

    }


}
