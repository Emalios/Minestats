package fr.emalios.mystats.impl.test;

import fr.emalios.mystats.MyStats;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.function.Predicate;

@GameTestHolder(MyStats.MODID)
public class HandlersTest {

    // Class name is prepended, template name is not specified
    // Template Location at 'modid:examplegametests.exampletest'
    @GameTest
    public static void exampleTest(GameTestHelper helper) { /*...*/ }

    // C:B:C
    // S
    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic")
    public static void chestBasic(GameTestHelper helper) {
        var barrel1 = new BlockPos(0, 1, 0);
        var chest = new BlockPos(1, 1, 0);
        var barrel2 = new BlockPos(2, 1, 0);

        helper.assertBlock(barrel1, Predicate.isEqual(Blocks.BARREL), "Should be first chest");
        helper.assertBlock(chest, Predicate.isEqual(Blocks.CHEST), "Should be barrel");
        helper.assertBlock(barrel2, Predicate.isEqual(Blocks.BARREL), "Should be second chest");
        helper.succeed();
    }

}
