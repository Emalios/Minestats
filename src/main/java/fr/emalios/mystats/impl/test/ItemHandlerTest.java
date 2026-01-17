package fr.emalios.mystats.impl.test;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Predicate;

@GameTestHolder(MyStats.MODID)
public class ItemHandlerTest {

    // C:B:C
    // S
    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic")
    public static void assertStructure(GameTestHelper helper) {
        var barrel1 = new BlockPos(0, 1, 0);
        var chest = new BlockPos(1, 1, 0);
        var barrel2 = new BlockPos(2, 1, 0);

        helper.assertBlock(barrel1, Predicate.isEqual(Blocks.BARREL), "Should be first barrel");
        helper.assertBlock(chest, Predicate.isEqual(Blocks.CHEST), "Should be chest");
        helper.assertBlock(barrel2, Predicate.isEqual(Blocks.BARREL), "Should be second barrel");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic")
    public static void getIItemHandler(GameTestHelper helper) {
        var barrel = new BlockPos(0, 1, 0);
        var chest = new BlockPos(1, 1, 0);
        var barrelAbsolute = helper.absolutePos(barrel);
        var chestAbsolute = helper.absolutePos(chest);
        var level = helper.getLevel();

        var blockEntity = level.getBlockEntity(barrelAbsolute);
        helper.assertTrue(blockEntity != null, "BlockEntity should exist");

        var capability = getItemHandler(level, barrelAbsolute);
        helper.assertTrue(capability != null, "ItemHandler should exist");

        var capCache = Utils.getCapabilityCache(level, barrelAbsolute, Capabilities.ItemHandler.BLOCK);
        helper.assertTrue(capCache.isPresent(), "Capability cache is empty");
        helper.assertFalse(capCache.get().getCapability() == null, "Capability found is null");

        var barrelHandlers = Utils.getIHandlers(level, barrelAbsolute);
        var chestHandlers = Utils.getIHandlers(level, chestAbsolute);
        helper.assertValueEqual(barrelHandlers.size(), 1, "barrel handlers should have one handler");
        helper.assertValueEqual(chestHandlers.size(), 1, "chest handlers should have one handler");
        helper.assertFalse(barrelHandlers.equals(chestHandlers), "barrel handlers should not have the same handler");

        helper.succeed();
    }

    //double chest does not have the same instance of the iitemhandler
    @PrefixGameTestTemplate(false)
    @GameTest(template = "double_chest")
    public static void getIITemHandlersFromDoubleChest(GameTestHelper helper) {
        var level = helper.getLevel();

        var chest1 = new BlockPos(0, 1, 0);
        var chest2 = new BlockPos(1, 1, 0);

        helper.assertBlock(chest1, Predicate.isEqual(Blocks.CHEST), "Should be chest");
        helper.assertBlock(chest2, Predicate.isEqual(Blocks.CHEST), "Should be chest");

        var chest1Absolute = helper.absolutePos(chest1);
        var chest2Absolute = helper.absolutePos(chest2);

        var cap1 = getItemHandler(level, chest1Absolute);
        var cap2 = getItemHandler(level, chest2Absolute);
        helper.assertTrue(cap1 != null, "Capability should exist");
        helper.assertTrue(cap2 != null, "Capability should exist");
        //helper.assertValueEqual(cap1, cap2, "Capabilities should be the same");

        var handlers1 = Utils.getIHandlers(level, chest1Absolute);
        var handlers2 = Utils.getIHandlers(level, chest2Absolute);
        //helper.assertValueEqual(handlers1, handlers2, "Handlers should be the same");

        helper.succeed();
    }

    private static IItemHandler getItemHandler(Level level, BlockPos blockPos) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, blockPos, null);
    }


}
