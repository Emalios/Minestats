package minestats.api.service;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.services.StatPlayerService;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.*;
import minestats.api.storage.DatabaseTest;
import minestats.api.storage.TestHandlerLoader;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

@DisplayName("InventoryService test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryServiceTest {

    private StatPlayerService statPlayerService;
    private InventoryService inventoryService;

    @BeforeAll
    void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        StatsAPI statsAPI = StatsAPI.getInstance();
        var playerInvRepo = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        statsAPI.init(
                new SqlitePlayerRepository(new PlayerDao(conn), playerInvRepo),
                new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn)),
                playerInvRepo,
                new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn)),
                new SqliteInventoryPositionsRepository(new InventoryPositionsDao(conn)), new TestHandlerLoader());
        this.statPlayerService = statsAPI.getPlayerService();
        this.inventoryService = statsAPI.getInventoryService();
    }


    @Test
    @DisplayName("Loaded inventories put in cache")
    public void loadedInventoriesPutInCacheTest() {
        Position pos1 = new Position("inv-cached", 0, 0, 0);
        Position pos2 = new Position("inv-cached", 0, 0, 1);
        Position pos3 = new Position("inv-cached", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);

        Assertions.assertTrue(this.inventoryService.isLoaded(inv1));
        Assertions.assertTrue(this.inventoryService.isLoaded(inv2));
    }

    @Test
    @DisplayName("Non existing inventory not in cache")
    public void nonExistingInventoryNotInCacheTest() {
        Position pos1 = new Position("null-inv-cached", 0, 0, 0);
        Position pos2 = new Position("null-inv-cached", 0, 0, 1);
        Position pos3 = new Position("null-inv-cached", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));

        Assertions.assertTrue(this.inventoryService.isLoaded(inv1));
        Assertions.assertFalse(this.inventoryService.isLoaded(inv2));
    }

    @Test
    @DisplayName("Find existing inventory")
    public void findExistingInventoryTest() {
        Position pos1 = new Position("find-inv-cached", 0, 0, 0);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);

        var optInv = this.inventoryService.findByPos(pos1);
        Assertions.assertTrue(optInv.isPresent());
        Assertions.assertTrue(inv1 == optInv.get());
    }

    @Test
    @DisplayName("Find existing inventory multi pos")
    public void findExistingInventoryMultiPosTest() {
        Position pos1 = new Position("find-inv-cached-multi-pos", 0, 0, 0);
        Position pos2 = new Position("find-inv-cached-multi-pos", 0, 0, 1);
        Position pos3 = new Position("find-inv-cached-multi-pos", 0, 0, 2);
        Set<Position> posSet = Set.of(pos1, pos2, pos3);
        Inventory inv1 = new Inventory(posSet);
        this.inventoryService.create(inv1);

        for (Position position : posSet) {
            var optInv = this.inventoryService.findByPos(position);
            Assertions.assertTrue(optInv.isPresent());
            Inventory inv = optInv.get();
            Assertions.assertEquals(posSet, inv.getInvPositions());
            Assertions.assertTrue(inv1 == inv);
        }
    }

    @Test
    @DisplayName("Find non existing inventory")
    public void findNonExistingInventoryTest() {
        Position pos1 = new Position("find-null-inv", 0, 0, 0);

        var optInv = this.inventoryService.findByPos(pos1);
        Assertions.assertFalse(optInv.isPresent());

    }

    @Test
    @DisplayName("Clear service")
    public void clearServiceTest() {
        Position pos1 = new Position("clear", 0, 0, 0);
        Position pos2 = new Position("clear", 0, 0, 1);
        Position pos3 = new Position("clear", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);

        var loadedInvs = new ArrayList<>(this.inventoryService.getAll());
        Assertions.assertFalse(loadedInvs.isEmpty());
        for (Inventory loadedInv : loadedInvs) {
            Assertions.assertTrue(loadedInv.isPersisted());
        }
        this.inventoryService.deleteAll();
        Assertions.assertTrue(this.inventoryService.getAll().isEmpty());
        for (Inventory loadedInv : loadedInvs) {
            Assertions.assertFalse(loadedInv.isPersisted());
        }
    }

    @Test
    @DisplayName("Delete inventory")
    public void deleteInventoryTest() {
        this.inventoryService.deleteAll();
        Position pos1 = new Position("delete-inv", 0, 0, 0);
        Position pos2 = new Position("delete-inv", 0, 0, 1);
        Position pos3 = new Position("delete-inv", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);

        this.inventoryService.deleteInventory(inv1);
        Assertions.assertFalse(inv1.isPersisted());

        var loadedInvs = new ArrayList<>(this.inventoryService.getAll());
        Assertions.assertFalse(loadedInvs.isEmpty());
        Assertions.assertEquals(1, loadedInvs.size());

        for (Inventory loadedInv : loadedInvs) {
            Assertions.assertTrue(loadedInv.isPersisted());
            Assertions.assertTrue(loadedInv == inv2);
        }
    }

    @Test
    @DisplayName("Same inventoriy instance")
    public void sameInventoriyInstanceTest() {
        this.inventoryService.deleteAll(); //due to getAll
        Position pos2 = new Position("inv-same-instance", 0, 0, 1);
        Position pos3 = new Position("inv-same-instance", 0, 0, 2);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);

        for (Inventory inventory : this.inventoryService.getAll()) {
            Assertions.assertTrue(inventory == inv2);
        }

    }

    @Test
    @DisplayName("Same inventories instance")
    public void sameInventoriesInstanceTest() {
        this.inventoryService.deleteAll(); //due to getAll
        Position pos1 = new Position("invs-same-instance", 0, 0, 0);
        Position pos2 = new Position("invs-same-instance", 0, 0, 1);
        Position pos3 = new Position("invs-same-instance", 0, 0, 2);
        Inventory inv1 = this.inventoryService.getOrCreate(pos1);
        Inventory inv2 = new Inventory(Set.of(pos2, pos3));
        this.inventoryService.create(inv2);

        for (Inventory inventory : this.inventoryService.getAll()) {
            Assertions.assertTrue(inventory == inv1 || inventory == inv2);
        }
        this.inventoryService.deleteAll();
    }

}
