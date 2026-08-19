package fr.emalios.minestats.impl.test.snippet;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
public class LevelTest {

    @GameTest(template = "empty")
    @PrefixGameTestTemplate(false)
    public static void getLevelName(GameTestHelper helper) {
        Level level = helper.getLevel();
        helper.assertValueEqual(level.dimension().location().toString(), "minecraft:overworld", "should be equal");
    }

}
