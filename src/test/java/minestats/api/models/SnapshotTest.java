package minestats.api.models;

import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.record.RecordType;
import fr.emalios.mystats.api.models.inventory.Snapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DisplayName("Record's test")
public class SnapshotTest {

    @Test
    @DisplayName("snapshot sort test")
    public void snapshotSortTest() throws InterruptedException {
        var list1 = List.of(new Record(RecordType.ITEM, "minecraft:stone", 1, CountUnit.ITEM));
        var list2 = List.of(new Record(RecordType.ITEM, "minecraft:dirt", 2, CountUnit.ITEM));
        var list3 = List.of(new Record(RecordType.ITEM, "minecraft:iron_ingot", 3, CountUnit.ITEM));
        Snapshot snapshot1 = new Snapshot(0, list1);
        TimeUnit.SECONDS.sleep(1);
        Snapshot snapshot2 = new Snapshot(0, list2);
        TimeUnit.SECONDS.sleep(1);
        Snapshot snapshot3 = new Snapshot(0, list3);
        var list = new ArrayList<>(List.of(snapshot3, snapshot1, snapshot2));
        Collections.sort(list);
        Assertions.assertEquals(list.get(0), snapshot1);
        Assertions.assertEquals(list.get(1), snapshot2);
        Assertions.assertEquals(list.get(2), snapshot3);
    }

}
