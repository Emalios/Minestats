package minestats.api;

import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.helper.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Conversion Test")
public class ConversionTest {

    @Test
    @DisplayName("mb to b divisible by 1000")
    void mBtoBDivisibleBy1000() {
        Pair<Float, CountUnit> result = CountUnit.simplify(1000, CountUnit.MB);
        assertEquals(1f, result.getA());
        assertEquals(CountUnit.B, result.getB());
    }

    @Test
    @DisplayName("mb to b non divisible by 1000")
    void mBtoBNonDivisibleBy1000() {
        Pair<Float, CountUnit> result = CountUnit.simplify(1500, CountUnit.MB);
        assertEquals(1.5f, result.getA());
        assertEquals(CountUnit.B, result.getB());
    }

    @Test
    @DisplayName("mb to mb < 1000")
    void mBtoBInferiorTo1000() {
        Pair<Float, CountUnit> result = CountUnit.simplify(999, CountUnit.MB);
        assertEquals(999f, result.getA());
        assertEquals(CountUnit.MB, result.getB());
    }

    @Test
    @DisplayName("mb to mb 0")
    void mBtoB0() {
        Pair<Float, CountUnit> result = CountUnit.simplify(0, CountUnit.MB);
        assertEquals(0f, result.getA());
        assertEquals(CountUnit.MB, result.getB());
    }

    @Test
    @DisplayName("mb to b negative")
    void mBtoBNegative() {
        Pair<Float, CountUnit> result = CountUnit.simplify(-1000, CountUnit.MB);
        assertEquals(-1f, result.getA());
        assertEquals(CountUnit.B, result.getB());
    }

    @Test
    @DisplayName("mb to mb -999")
    void mBtoBMinus999() {
        Pair<Float, CountUnit> result = CountUnit.simplify(-999, CountUnit.MB);
        assertEquals(-999f, result.getA());
        assertEquals(CountUnit.MB, result.getB());
    }

}
