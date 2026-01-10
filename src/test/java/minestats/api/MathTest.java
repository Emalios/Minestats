package minestats.api;

import fr.emalios.mystats.api.*;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.stat.Stat;
import fr.emalios.mystats.api.stat.utils.StatCalculator;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.*;
import fr.emalios.mystats.impl.storage.repository.SqliteInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqliteInventorySnapshotRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.repository.SqlitePlayerRepository;
import minestats.api.storage.DatabaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("Math test")
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

    @BeforeAll
    public static void setup() {
        Connection conn = DatabaseTest.getConnection();
        Storage.registerInventorySnapshotRepo(new SqliteInventorySnapshotRepository(new InventorySnapshotDao(conn), new RecordDao(conn)));
        Storage.registerPlayerInventoriesRepo(new SqlitePlayerInventoryRepository(new PlayerInventoryDao(conn)));
        Storage.registerPlayerRepo(new SqlitePlayerRepository(new PlayerDao(conn)));
        Storage.registerInventoryRepo(new SqliteInventoryRepository(new InventoryDao(conn)));
    }


    @Test
    @DisplayName("Two snapshot test")
    public void twoSnapshotTest() throws InterruptedException {
        StatPlayer player = Storage.players().getOrCreate("test-stat-math-two-snapshot");
        Inventory inv1 = Storage.inventories().getOrCreate("test-stat-math-two-snapshot", 0, 0, 0);
        count = 1;
        inv1.addHandler(basicHandler);
        inv1.recordContent();
        count++;

        TimeUnit.SECONDS.sleep(1);
        inv1.recordContent();

        player.addInventory(inv1);
        var result = StatCalculator.getInstance().genPerSecond(player.getInventories());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 10, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 64, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 1000, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("ten snapshot test")
    public void tenSnapshotTest() throws InterruptedException {
        StatPlayer player = Storage.players().getOrCreate("test-stat-math-ten-snapshot");
        Inventory inv1 = Storage.inventories().getOrCreate("test-stat-math-ten-snapshot", 0, 0, 0);
        inv1.addHandler(basicHandler);
        count = 1;
        //non linear values to be able to see if stats are made with these lines (5 lines)
        for (int i = 0; i < 5; i++) {
            inv1.recordContent();
            count*=10;
            TimeUnit.SECONDS.sleep(1);
        }
        //linear values to make stats with (10 lines)
        count = 1;
        for (int i = 0; i < 10; i++) {
            inv1.recordContent();
            count++;
            TimeUnit.SECONDS.sleep(1);
        }

        player.addInventory(inv1);
        var result = StatCalculator.getInstance().genPerSecond(player.getInventories());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 10, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 64, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 1000, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("Negative result")
    public void negativeStat() throws InterruptedException {
        StatPlayer player = Storage.players().getOrCreate("test-stat-math-negative");
        Inventory inv1 = Storage.inventories().getOrCreate("test-stat-math-negative", 0, 0, 0);
        count = -1;
        inv1.addHandler(basicHandler);
        inv1.recordContent();
        count--;

        TimeUnit.SECONDS.sleep(1);
        inv1.recordContent();

        player.addInventory(inv1);
        var result = StatCalculator.getInstance().genPerSecond(player.getInventories());

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", -10, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", -64, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", -1000, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("zero result")
    public void zeroResult() throws InterruptedException {
        StatPlayer player = Storage.players().getOrCreate("test-stat-math-zero");
        Inventory inv1 = Storage.inventories().getOrCreate("test-stat-math-zero", 0, 0, 0);
        count = 1;
        inv1.addHandler(basicHandler);
        inv1.recordContent();

        TimeUnit.SECONDS.sleep(1);
        inv1.recordContent();

        player.addInventory(inv1);
        var result = StatCalculator.getInstance().genPerSecond(player.getInventories());
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 0, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 0, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 0, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("multi inv test")
    public void multiInvMerge() throws InterruptedException {
        int numberOfInvs = 2;
        StatPlayer player = Storage.players().getOrCreate("test-stat-math-multi-inv-snapshot");
        Inventory inv1 = Storage.inventories().getOrCreate("test-stat-math-multi-1", 0, 0, 0);
        Inventory inv2 = Storage.inventories().getOrCreate("test-stat-math-multi-2", 0, 0, 0);
        inv1.addHandler(basicHandler);
        inv2.addHandler(basicHandler);
        count = 1;
        //non linear values to be able to see if stats are made with these lines (5 lines)
        for (int i = 0; i < 5; i++) {
            inv1.recordContent();
            inv2.recordContent();
            count*=10;
            TimeUnit.SECONDS.sleep(1);
        }
        //linear values to make stats with (10 lines)
        count = 1;
        for (int i = 0; i < 10; i++) {
            inv1.recordContent();
            inv2.recordContent();
            count++;
            TimeUnit.SECONDS.sleep(1);
        }

        player.addInventory(inv1);
        player.addInventory(inv2);
        var result = StatCalculator.getInstance().genPerSecond(player.getInventories());
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", numberOfInvs*10, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", numberOfInvs*64, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", numberOfInvs*1000, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

}
