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

    private static List<Record> genITem() {
        return List.of(
                new Record(RecordType.ITEM, "minecraft:dirt", 10*count, CountUnit.ITEM),
                new Record(RecordType.ITEM, "minecraft:stone", 64*count, CountUnit.ITEM),
                new Record(RecordType.FLUID, "minecraft:water", 1000*count, CountUnit.MB)
        );
    }

    private static IHandler basicItemHandler = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return genITem();
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
        StatPlayer player = Storage.players().getOrCreate("test-stat-math");
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 0, 0, 0);
        count = 1;
        inv1.addHandler(basicItemHandler);
        inv1.recordContent();
        count++;

        TimeUnit.SECONDS.sleep(1);
        inv1.recordContent();
        count++;

        player.addInventory(inv1);
        var result = StatCalculator.getInstance().genPerSecond(player.getInventories());
        System.out.println(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 10, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 64, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 1000, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

    @Test
    @DisplayName("ten snapshot test")
    public void tenSnapshotTest() throws InterruptedException {
        StatPlayer player = Storage.players().getOrCreate("test-stat-math");
        Inventory inv1 = Storage.inventories().getOrCreate("minecraft:overworld", 0, 0, 0);
        inv1.addHandler(basicItemHandler);
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
        System.out.println(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:dirt", 10, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.ITEM, "minecraft:stone", 64, CountUnit.ITEM), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
        Assertions.assertTrue(result.contains(new Stat(new Record(RecordType.FLUID, "minecraft:water", 1000, CountUnit.MB), fr.emalios.mystats.api.stat.TimeUnit.SECOND)));
    }

}
