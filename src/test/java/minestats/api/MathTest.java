package minestats.api;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.*;
import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.models.record.RecordType;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.services.StatCalculatorService;
import fr.emalios.mystats.api.services.StatPlayerService;
import fr.emalios.mystats.api.models.inventory.IHandler;
import fr.emalios.mystats.api.models.stat.Stat;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.*;
import minestats.api.storage.DatabaseTest;
import minestats.api.storage.TestHandlerLoader;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("Math test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MathTest {

    private static int count = 1;

    private static List<Record> basicGen() {
        return List.of(
                new Record(RecordType.ITEM, "minecraft:dirt", 10*count, CountUnit.ITEM),
                new Record(RecordType.ITEM, "minecraft:stone", 64*count, CountUnit.ITEM),
                new Record(RecordType.FLUID, "minecraft:water", 1000*count, CountUnit.MB)
        );
    }

    private static IHandler basicHandler = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return basicGen();
        }
    };

    private StatPlayerService playerService;
    private InventoryService inventoryService;
    private StatCalculatorService statCalculatorService;

    @BeforeAll
    public void setup() throws SQLException {
        Connection conn = DatabaseTest.getConnection();
        DatabaseTest.makeMigrations();
        StatsAPI statsAPI = StatsAPI.getInstance();
        var playerInventoryRepository = new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn));
        var playerRepository = new SqlitePlayerRepository(new PlayerDao(conn), playerInventoryRepository);
        var inventoryRepository = new SqliteInventoryRepository(new InventoryDao(conn), new InventoryPositionsDao(conn));
        var inventorySnapshotRepository = new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn));
        var inventoryPositionsRepository = new SqliteInventoryPositionsRepository(new InventoryPositionsDao(conn));
        statsAPI.init(playerRepository, inventoryRepository, playerInventoryRepository, inventorySnapshotRepository, inventoryPositionsRepository, new TestHandlerLoader());
        this.inventoryService = statsAPI.getInventoryService();
        this.playerService = statsAPI.getPlayerService();
        this.statCalculatorService = statsAPI.getStatCalculatorService();
    }

    @AfterAll
    public void teardown() {
        DatabaseTest.close();
    }


    @Test
    @DisplayName("Two snapshot test")
    public void twoSnapshotTest() throws InterruptedException {
        StatPlayer player = this.playerService.getOrCreateByName("test-stat-math-two-snapshot");
        Inventory inv1 = this.inventoryService.getOrCreate(new Position("test-stat-math-two-snapshot", 0, 0, 0));
        count = 1;
        inv1.addHandler(basicHandler);
        this.inventoryService.recordInventoryContent(inv1);
        count++;

        TimeUnit.SECONDS.sleep(1);
        this.inventoryService.recordInventoryContent(inv1);

        player.addInventory(inv1);
        var result = this.statCalculatorService.genPerSecond(player.getInventories());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 10, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 64, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 1000, CountUnit.MB), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("ten snapshot test")
    public void tenSnapshotTest() throws InterruptedException {
        StatPlayer player = this.playerService.getOrCreateByName("test-stat-math-ten-snapshot");
        Inventory inv1 = this.inventoryService.getOrCreate(new Position("test-stat-math-ten-snapshot", 0, 0, 0));
        inv1.addHandler(basicHandler);
        count = 1;
        //non linear values to be able to see if stats are made with these lines (5 lines)
        for (int i = 0; i < 5; i++) {
            this.inventoryService.recordInventoryContent(inv1);
            count*=10;
            TimeUnit.SECONDS.sleep(1);
        }
        //linear values to make stats with (10 lines)
        count = 1;
        for (int i = 0; i < 10; i++) {
            this.inventoryService.recordInventoryContent(inv1);
            count++;
            TimeUnit.SECONDS.sleep(1);
        }

        player.addInventory(inv1);
        var result = this.statCalculatorService.genPerSecond(player.getInventories());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 10, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 64, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 1000, CountUnit.MB), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("Negative result")
    public void negativeStat() throws InterruptedException {
        StatPlayer player = this.playerService.getOrCreateByName("test-stat-math-negative");
        Inventory inv1 = this.inventoryService.getOrCreate(new Position("test-stat-math-negative", 0, 0, 0));
        count = -1;
        inv1.addHandler(basicHandler);
        this.inventoryService.recordInventoryContent(inv1);
        count--;

        TimeUnit.SECONDS.sleep(1);
        this.inventoryService.recordInventoryContent(inv1);

        player.addInventory(inv1);
        var result = this.statCalculatorService.genPerSecond(player.getInventories());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", -10, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", -64, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", -1000, CountUnit.MB), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("zero result")
    public void zeroResult() throws InterruptedException {
        StatPlayer player = this.playerService.getOrCreateByName("test-stat-math-zero");
        Inventory inv1 = this.inventoryService.getOrCreate(new Position("test-stat-math-zero", 0, 0, 0));
        count = 1;
        inv1.addHandler(basicHandler);
        this.inventoryService.recordInventoryContent(inv1);

        TimeUnit.SECONDS.sleep(1);
        this.inventoryService.recordInventoryContent(inv1);

        player.addInventory(inv1);
        var result = this.statCalculatorService.genPerSecond(player.getInventories());
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 0, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 0, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 0, CountUnit.MB), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("multi inv test")
    public void multiInvMerge() throws InterruptedException {
        int numberOfInvs = 2;
        StatPlayer player = this.playerService.getOrCreateByName("test-stat-math-multi-inv-snapshot");
        Inventory inv1 = this.inventoryService.getOrCreate(new Position("test-stat-math-multi-1", 0, 0, 0));
        Inventory inv2 = this.inventoryService.getOrCreate(new Position("test-stat-math-multi-2", 0, 0, 0));
        inv1.addHandler(basicHandler);
        inv2.addHandler(basicHandler);
        count = 1;
        //non linear values to be able to see if stats are made with these lines (5 lines)
        for (int i = 0; i < 5; i++) {
            this.inventoryService.recordInventoryContent(inv1);
            this.inventoryService.recordInventoryContent(inv2);
            count*=10;
            TimeUnit.SECONDS.sleep(1);
        }
        //linear values to make stats with (10 lines)
        count = 1;
        for (int i = 0; i < 10; i++) {
            this.inventoryService.recordInventoryContent(inv1);
            this.inventoryService.recordInventoryContent(inv2);
            count++;
            TimeUnit.SECONDS.sleep(1);
        }

        player.addInventory(inv1);
        player.addInventory(inv2);
        var result = this.statCalculatorService.genPerSecond(player.getInventories());
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", numberOfInvs*10, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", numberOfInvs*64, CountUnit.ITEM), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", numberOfInvs*1000, CountUnit.MB), fr.emalios.mystats.api.models.stat.TimeUnit.SECOND)));
    }

}
