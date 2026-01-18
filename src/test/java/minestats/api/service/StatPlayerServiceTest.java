package minestats.api.service;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.models.StatPlayer;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.services.StatPlayerService;
import minestats.api.TestStatsAPI;
import org.junit.jupiter.api.*;

import java.util.Optional;
import java.util.Set;

@DisplayName("StatPlayerService test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StatPlayerServiceTest {

    private StatPlayerService statPlayerService;
    private InventoryService inventoryService;

    @BeforeAll
    void setup() {
        StatsAPI statsAPI = TestStatsAPI.getInstance();
        statsAPI.init();
        this.statPlayerService = statsAPI.getPlayerService();
        this.inventoryService = statsAPI.getInventoryService();
    }

    @AfterAll
    void teardown() {
        TestStatsAPI.getInstance().close();
    }

    @Test
    @DisplayName("Create player")
    void addPlayerTest() {
        StatPlayer player = this.statPlayerService.getOrCreateByName("create_player");
        Assertions.assertTrue(this.statPlayerService.isLoaded(player.getName()));

        Assertions.assertTrue(player.isPersisted());
        Assertions.assertNotNull(player.getId());
        Assertions.assertEquals("create_player", player.getName());
    }

    @Test
    @DisplayName("Get existing player")
    void getExistingPlayerTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("get-existing-player");
        Assertions.assertTrue(this.statPlayerService.isLoaded(p1.getName()));
        Optional<StatPlayer> optP2 = this.statPlayerService.findByName("get-existing-player");
        Assertions.assertTrue(optP2.isPresent());
        StatPlayer p2 = optP2.get();
        Assertions.assertTrue(p1.isPersisted());
        Assertions.assertTrue(p2.isPersisted());
        Assertions.assertEquals(p1.getId(), p2.getId());
        Assertions.assertEquals(p1.getName(), p2.getName());
        Assertions.assertEquals(p1, p2);
    }

    @Test
    @DisplayName("Get existing player in cache")
    void getExistingPlayerInCacheTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("get-existing-player-cache");
        Assertions.assertTrue(this.statPlayerService.isLoaded(p1.getName()));
        StatPlayer p2 = this.statPlayerService.getOrCreateByName("get-existing-player-cache");
        Assertions.assertTrue(p1 == p2);
    }

    @Test
    @DisplayName("Find existing player in cache")
    void findExistingPlayerInCacheTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("find-existing-player-cache");
        Assertions.assertTrue(this.statPlayerService.isLoaded(p1.getName()));
        var optPlayer = this.statPlayerService.findByName("find-existing-player-cache");
        Assertions.assertTrue(optPlayer.isPresent());
        Assertions.assertTrue(p1 == optPlayer.get());
    }

    @Test
    @DisplayName("Find non existing player")
    void findNonExistingPlayerTest() {
        var optPlayer = this.statPlayerService.findByName("find-non-existing-player");
        Assertions.assertFalse(optPlayer.isPresent());
        Assertions.assertFalse(this.statPlayerService.isLoaded("find-non-existing-player"));
    }

    @Test
    @DisplayName("Get non existing player")
    void getNonExistingPlayerTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("base-player");
        Optional<StatPlayer> optP2 = this.statPlayerService.findByName("Null");
        Assertions.assertFalse(this.statPlayerService.isLoaded("Null"));
        Assertions.assertTrue(optP2.isEmpty());
    }

    @Test
    @DisplayName("Add single inventory")
    void addSingleInventoryTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("add-single-inventory-player");
        Position pos1 = new Position("add-single", 0, 0, 0);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        this.statPlayerService.addInventoryToPlayer(p1, inv1);

        var playerInvs = p1.getInventories();
        Assertions.assertEquals(1, playerInvs.size());
        Assertions.assertTrue(playerInvs.contains(inv1));
        Assertions.assertTrue(p1.hasInventory(inv1));

        //test same instance of inventories
        for (Inventory playerInv : playerInvs) {
            Assertions.assertTrue(playerInv == inv1);
        }
    }

    @Test
    @DisplayName("Add inventories")
    void addInventoryTest() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("add-inventories-player");
        Position pos1 = new Position("inv1", 0, 0, 0);
        Position pos2 = new Position("inv1", 0, 0, 1);
        Position pos3 = new Position("inv1", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);
        this.statPlayerService.addInventoryToPlayer(p1, inv1);
        this.statPlayerService.addInventoryToPlayer(p1, inv2);

        var playerInvs = p1.getInventories();
        Assertions.assertEquals(2, playerInvs.size());
        Assertions.assertTrue(playerInvs.contains(inv1));
        Assertions.assertTrue(playerInvs.contains(inv2));
        Assertions.assertTrue(p1.hasInventory(inv1));
        Assertions.assertTrue(p1.hasInventory(inv2));

        //test same instance of inventories
        for (Inventory playerInv : playerInvs) {
            Assertions.assertTrue(playerInv == inv1 || playerInv == inv2);
        }
    }

    @Test
    @DisplayName("Load inventories from storage")
    void loadInventoriesFromDb() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("load-inventories-player");
        Position pos1 = new Position("load-inventories", 0, 0, 0);
        Position pos2 = new Position("load-inventories", 0, 0, 1);
        Position pos3 = new Position("load-inventories", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);
        this.statPlayerService.addInventoryToPlayer(p1, inv1);
        this.statPlayerService.addInventoryToPlayer(p1, inv2);

        p1 =  this.statPlayerService.getOrCreateByName("load-inventories-player");
        var playerInvs = p1.getInventories();
        Assertions.assertEquals(2, playerInvs.size());
        Assertions.assertTrue(playerInvs.contains(inv1));
        Assertions.assertTrue(playerInvs.contains(inv2));
        Assertions.assertTrue(p1.hasInventory(inv1));
        Assertions.assertTrue(p1.hasInventory(inv2));

        //test same instance of inventories
        for (Inventory playerInv : playerInvs) {
            Assertions.assertTrue(playerInv == inv1 || playerInv == inv2);
        }
    }

    @Test
    @DisplayName("Remove inventories from player")
    void removeInventoriesFromPlayer() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("remove-inventories-player");
        Position pos1 = new Position("remove-inv-from-player", 0, 0, 0);
        Position pos2 = new Position("remove-inv-from-player", 0, 0, 1);
        Position pos3 = new Position("remove-inv-from-player", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);
        this.statPlayerService.addInventoryToPlayer(p1, inv1);
        this.statPlayerService.addInventoryToPlayer(p1, inv2);

        this.statPlayerService.removeInventoryFromPlayer(p1, inv1);
        this.statPlayerService.removeInventoryFromPlayer(p1, inv2);
        Assertions.assertFalse(p1.hasInventory(inv1));
        Assertions.assertFalse(p1.hasInventory(inv2));
        Assertions.assertEquals(0, p1.getInventories().size());
    }

    @Test
    @DisplayName("Remove non existing inventory from player")
    void removeNonExistingInventoryFromPlayer() {
        StatPlayer p1 = this.statPlayerService.getOrCreateByName("remove-inventories-player");
        Position pos1 = new Position("remove-null-inventories", 0, 0, 0);
        Position pos2 = new Position("remove-null-inventories", 0, 0, 1);
        Position pos3 = new Position("remove-null-inventories", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);
        this.statPlayerService.addInventoryToPlayer(p1, inv1);

        boolean deleted = this.statPlayerService.removeInventoryFromPlayer(p1, inv2);
        Assertions.assertFalse(deleted);
        Assertions.assertFalse(p1.hasInventory(inv2));
        Assertions.assertEquals(1, p1.getInventories().size());
    }

}
