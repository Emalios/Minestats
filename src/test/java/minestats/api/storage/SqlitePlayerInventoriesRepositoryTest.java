package minestats.api.storage;

import fr.emalios.mystats.api.models.Inventory;
import fr.emalios.mystats.api.models.Position;
import fr.emalios.mystats.api.models.StatPlayer;
import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.api.storage.PlayerInventoryRepository;
import fr.emalios.mystats.api.storage.PlayerRepository;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;
import fr.emalios.mystats.impl.storage.dao.InventoryPositionsDao;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@DisplayName("PlayerInventoriesRepository test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SqlitePlayerInventoriesRepositoryTest {
    
    private PlayerInventoryRepository playerInventoryRepository;
    private PlayerRepository playerRepository;
    private InventoryRepository inventoryRepository;

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        this.playerInventoryRepository = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        this.playerRepository = new SqlitePlayerRepository(new PlayerDao(conn), this.playerInventoryRepository);
        this.inventoryRepository = new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn));
    }

    @AfterAll
    void teardown() {
        DatabaseTest.close();
    }

    @Test
    @DisplayName("Assign inventory")
    void assignInventoryTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("assign-classic-player");
        Inventory inventory = this.inventoryRepository.getOrCreate(new Position("minecraft:overworld", 0, 1, 2));

        this.playerInventoryRepository.addInventory(statPlayer, inventory);

        Assertions.assertTrue(statPlayer.hasInventory(inventory));
        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inventory));

        StatPlayer updatedPlayer = this.playerRepository.getOrCreate("assign-classic-player");
        Assertions.assertTrue(updatedPlayer.hasInventory(inventory));
        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(updatedPlayer, inventory));
    }

    @Test
    @DisplayName("Assign multiple inventories")
    void assignMultipleInventoriesTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("player1");
        Inventory inv1 = this.inventoryRepository.getOrCreate(new Position("minecraft:overworld", 0, 1, 2));
        Inventory inv2 = this.inventoryRepository.getOrCreate(new Position("minecraft:nether", 6, 17, 22));
        Inventory inv3 = this.inventoryRepository.getOrCreate(new Position("minecraft:end", 96, 54, 29));

        this.playerInventoryRepository.addInventory(statPlayer, inv1);
        this.playerInventoryRepository.addInventory(statPlayer, inv2);
        this.playerInventoryRepository.addInventory(statPlayer, inv3);

        Assertions.assertTrue(statPlayer.hasInventory(inv1));
        Assertions.assertTrue(statPlayer.hasInventory(inv2));
        Assertions.assertTrue(statPlayer.hasInventory(inv3));

        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inv1));
        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inv2));
        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inv3));
    }

    @Test
    @DisplayName("Assign to StatPlayer should not modify db")
    void assignToStatPlayerShouldNotModifyDb() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("assign-to-player");
        Inventory inv1 = this.inventoryRepository.getOrCreate(new Position("assign-to-player-inv-1", 0, 1, 2));
        Inventory inv2 = this.inventoryRepository.getOrCreate(new Position("assign-to-player-inv-2", 6, 17, 22));
        Inventory inv3 = this.inventoryRepository.getOrCreate(new Position("assign-to-player-inv-3", 96, 54, 29));

        statPlayer.addInventory(inv1);
        statPlayer.addInventory(inv2);
        statPlayer.addInventory(inv3);

        Assertions.assertTrue(statPlayer.hasInventory(inv1));
        Assertions.assertTrue(statPlayer.hasInventory(inv2));
        Assertions.assertTrue(statPlayer.hasInventory(inv3));

        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inv1));
        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inv2));
        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inv3));
    }

    @Test
    @DisplayName("Has unstored inventory")
    void hasUnstoredInventoryTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("player1");
        Position position = new Position("has-unstored-inventory", 0, 0, 0);
        Inventory inv1 = this.inventoryRepository.getOrCreate(position);

        this.playerInventoryRepository.addInventory(statPlayer, inv1);

        Inventory inv2 = new Inventory(Set.of(position));
        Assertions.assertTrue(statPlayer.hasInventory(inv2));
    }

    @Test
    @DisplayName("Assign non persisted inventory")
    void assignNonPersistedInventoryTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("player1");
        Inventory inventory = new Inventory(Set.of(new Position("minecraft:overworld", 0, 1, 2)));

        Assertions.assertThrows(IllegalStateException.class, () -> this.playerInventoryRepository.addInventory(statPlayer, inventory));
    }

    @Test
    @DisplayName("Assign non persisted player")
    void assignNonPersistedPlayerTest() {
        StatPlayer statPlayer = new StatPlayer("player1");
        Inventory inventory = new Inventory(Set.of(new Position("minecraft:overworld", 0, 1, 2)));

        Assertions.assertThrows(IllegalStateException.class, () -> this.playerInventoryRepository.addInventory(statPlayer, inventory));
    }

    @Test
    @DisplayName("Remove assigned inventory")
    void removeInventoryTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("player1");
        Inventory inventory = this.inventoryRepository.getOrCreate(new Position("minecraft:overworld", 0, 1, 2));

        this.playerInventoryRepository.addInventory(statPlayer, inventory);
        Assertions.assertTrue(statPlayer.hasInventory(inventory));

        boolean deleted = this.playerInventoryRepository.removeInventory(statPlayer, inventory);
        Assertions.assertTrue(deleted);
        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inventory));
        Assertions.assertFalse(statPlayer.hasInventory(inventory));
    }

    @Test
    @DisplayName("Remove unassigned inventory")
    void removeUnassignedInventoryTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("player1");
        Inventory inventory = this.inventoryRepository.getOrCreate(new Position("minecraft:nether", 0, 1, 2));

        boolean deleted = this.playerInventoryRepository.removeInventory(statPlayer, inventory);
        Assertions.assertFalse(deleted);
    }

    @Test
    @DisplayName("Remove multiple inventories")
    void removeMultipleInventoriesTest() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("player1");
        Inventory inv1 = this.inventoryRepository.getOrCreate(new Position("minecraft:overworld", 0, 1, 2));
        Inventory inv2 = this.inventoryRepository.getOrCreate(new Position("minecraft:nether", 6, 17, 22));
        Inventory inv3 = this.inventoryRepository.getOrCreate(new Position("minecraft:end", 96, 54, 29));

        this.playerInventoryRepository.addInventory(statPlayer, inv1);
        this.playerInventoryRepository.addInventory(statPlayer, inv2);
        this.playerInventoryRepository.addInventory(statPlayer, inv3);

        Assertions.assertTrue(this.playerInventoryRepository.removeInventory(statPlayer, inv1));
        Assertions.assertTrue(this.playerInventoryRepository.removeInventory(statPlayer, inv2));
        Assertions.assertTrue(this.playerInventoryRepository.removeInventory(statPlayer, inv3));

        Assertions.assertFalse(statPlayer.hasInventory(inv1));
        Assertions.assertFalse(statPlayer.hasInventory(inv2));
        Assertions.assertFalse(statPlayer.hasInventory(inv3));

        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inv1));
        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inv2));
        Assertions.assertFalse(this.playerInventoryRepository.hasInventory(statPlayer, inv3));

    }

    @Test
    @DisplayName("Remove to StatPlayer should not modify db")
    void removeToStatPlayerShouldNotModifyDb() {
        StatPlayer statPlayer = this.playerRepository.getOrCreate("remove-to-player");
        Inventory inv1 = this.inventoryRepository.getOrCreate(new Position("remove-to-player-inv-1", 0, 1, 2));
        Inventory inv2 = this.inventoryRepository.getOrCreate(new Position("remove-to-player-inv-2", 6, 17, 22));
        Inventory inv3 = this.inventoryRepository.getOrCreate(new Position("remove-to-player-inv-3", 96, 54, 29));

        this.playerInventoryRepository.addInventory(statPlayer, inv1);
        this.playerInventoryRepository.addInventory(statPlayer, inv2);
        this.playerInventoryRepository.addInventory(statPlayer, inv3);

        statPlayer.removeInventory(inv1);
        statPlayer.removeInventory(inv2);
        statPlayer.removeInventory(inv3);

        Assertions.assertFalse(statPlayer.hasInventory(inv1));
        Assertions.assertFalse(statPlayer.hasInventory(inv2));
        Assertions.assertFalse(statPlayer.hasInventory(inv3));

        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inv1));
        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inv2));
        Assertions.assertTrue(this.playerInventoryRepository.hasInventory(statPlayer, inv3));
    }

    @Test
    @DisplayName("Test tricky")
    void tricky() {
        StatPlayer player1 = this.playerRepository.getOrCreate("tricky-player-1");
        Position pos1 = new Position("tricky-player-pos-1", 0, 0, 0);
        Position pos2 = new Position("tricky-player-pos-2", 1, 0, 0);
        Position pos3 = new Position("tricky-player-pos-3", 1, 0, 1);
        Position pos4 = new Position("tricky-player-pos-4", 0, 0, 1);

        Set<Position> set = new HashSet<>();
        set.add(pos1);
        set.add(pos2);
        set.add(pos3);
        set.add(pos4);

        Inventory inv = new Inventory(set);
        this.inventoryRepository.save(inv);

        this.playerInventoryRepository.addInventory(player1, inv);

        StatPlayer player2 = this.playerRepository.getOrCreate("tricky-player-2");
    }


}
