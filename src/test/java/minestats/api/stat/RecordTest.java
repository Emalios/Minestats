package minestats.api.stat;

import fr.emalios.mystats.api.stat.CountUnit;
import fr.emalios.mystats.api.stat.Record;
import fr.emalios.mystats.api.stat.RecordType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Record's test")
public class RecordTest {

    private final Record dirt1 = new Record(RecordType.ITEM, "minecraft:dirt", 1, CountUnit.ITEM);
    private final Record dirt64 = new Record(RecordType.ITEM, "minecraft:dirt", 64, CountUnit.ITEM);
    private final Record stone1 = new Record(RecordType.ITEM, "minecraft:stone", 1, CountUnit.ITEM);
    private final Record water1 = new Record(RecordType.FLUID, "minecraft:water", 1, CountUnit.MB);
    private final Record water100 = new Record(RecordType.FLUID, "minecraft:water", 100, CountUnit.MB);
    private final Record water1000 = new Record(RecordType.FLUID, "minecraft:water", 1, CountUnit.B);
    private final Record lava1 = new Record(RecordType.FLUID, "minecraft:lava", 1, CountUnit.MB);

    @Test
    @DisplayName("merge null")
    public void mergeNull() {
        assertThrows(NullPointerException.class, () -> {
            dirt1.mergeWith(null);
        });
    }

    @Test
    @DisplayName("merge dirt with dirt")
    public void mergeDirtWithDirt() {
        Record result65 = new Record(RecordType.ITEM, "minecraft:dirt", 65, CountUnit.ITEM);
        assertEquals(result65, dirt1.mergeWith(dirt64));
    }

    @Test
    @DisplayName("merge dirt with stone")
    public void mergeDirtWithStone() {
        assertThrows(IllegalArgumentException.class, () -> dirt1.mergeWith(stone1));
    }

    @Test
    @DisplayName("merge dirt with water")
    public void mergeDirtWithWater() {
        assertThrows(IllegalArgumentException.class, () -> dirt1.mergeWith(water1));
    }

    @Test
    @DisplayName("merge dirt with dirt stack")
    public void mergeDirtWithDirtStack() {
        assertThrows(IllegalArgumentException.class, () -> dirt1.mergeWith(stone1));
    }

    @Test
    @DisplayName("merge water with water")
    public void mergeWaterWithWater() {
        Record water101 = new Record(RecordType.FLUID, "minecraft:water", 101, CountUnit.MB);
        assertEquals(water101, water1.mergeWith(water100));
    }

    @Test
    @DisplayName("merge water with water 1000")
    public void mergeWaterWithWater1000() {
        assertThrows(IllegalArgumentException.class, () -> water1.mergeWith(water1000));
    }

    @Test
    @DisplayName("merge water with lava")
    public void mergeWaterWithLava() {
        assertThrows(IllegalArgumentException.class, () -> water1.mergeWith(lava1));
    }
}
