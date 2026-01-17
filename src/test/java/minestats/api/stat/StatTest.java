package minestats.api.stat;

import fr.emalios.mystats.api.stat.*;
import fr.emalios.mystats.api.models.CountUnit;
import fr.emalios.mystats.api.models.RecordType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Stat's test")
public class StatTest {

    private final Stat dirt1 = new Stat(RecordType.ITEM, "minecraft:dirt", 1, CountUnit.ITEM, TimeUnit.SECOND);
    private final Stat dirt64 = new Stat(RecordType.ITEM, "minecraft:dirt", 64, CountUnit.ITEM, TimeUnit.SECOND);
    private final Stat dirt64Minute = new Stat(RecordType.ITEM, "minecraft:dirt", 64, CountUnit.ITEM, TimeUnit.MINUTE);
    private final Stat stone1 = new Stat(RecordType.ITEM, "minecraft:stone", 1, CountUnit.ITEM, TimeUnit.SECOND);
    private final Stat water1 = new Stat(RecordType.FLUID, "minecraft:water", 1, CountUnit.MB, TimeUnit.SECOND);
    private final Stat water100 = new Stat(RecordType.FLUID, "minecraft:water", 100, CountUnit.MB, TimeUnit.SECOND);
    private final Stat water1000 = new Stat(RecordType.FLUID, "minecraft:water", 1, CountUnit.B, TimeUnit.SECOND);
    private final Stat lava1 = new Stat(RecordType.FLUID, "minecraft:lava", 1, CountUnit.MB, TimeUnit.SECOND);

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
        Stat result65 = new Stat(RecordType.ITEM, "minecraft:dirt", 65, CountUnit.ITEM, TimeUnit.SECOND);
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
    @DisplayName("merge dirt with dirt in minute")
    public void mergeDirtWithDirtMinute() {
        assertThrows(IllegalArgumentException.class, () -> dirt1.mergeWith(dirt64Minute));
    }

    @Test
    @DisplayName("merge dirt with dirt stack")
    public void mergeDirtWithDirtStack() {
        assertThrows(IllegalArgumentException.class, () -> dirt1.mergeWith(stone1));
    }

    @Test
    @DisplayName("merge water with water")
    public void mergeWaterWithWater() {
        Stat water101 = new Stat(RecordType.FLUID, "minecraft:water", 101, CountUnit.MB, TimeUnit.SECOND);
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
