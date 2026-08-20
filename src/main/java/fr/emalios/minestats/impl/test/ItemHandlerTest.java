package fr.emalios.minestats.impl.test;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.IHandler;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.helper.Utils;
import fr.emalios.minestats.impl.McStatsAPI;
import fr.emalios.minestats.impl.adapter.ItemAdapter;
import fr.emalios.minestats.impl.adapter.McHandlersLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Test if IItemHandlers are gotten rightly
 */
@GameTestHolder(MineStats.MODID)
public class ItemHandlerTest {

    private static final StatsAPI statsApi = McStatsAPI.getInstance();

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

        var barrelBlockEntity = level.getBlockEntity(barrelAbsolute);
        helper.assertTrue(barrelBlockEntity != null, "BlockEntity should exist");

        var chestBlockEntity = level.getBlockEntity(chestAbsolute);
        helper.assertTrue(chestBlockEntity != null, "BlockEntity should exist");

        var barrelHandlers = Utils.getIHandlers(level, barrelAbsolute);
        helper.assertValueEqual(barrelHandlers.size(), 1, "barrel handlers should have one handler");
        helper.assertTrue(barrelHandlers.get(0) instanceof ItemAdapter, "barrel handlers should only has ItemHandler");

        var chestHandlers = Utils.getIHandlers(level, chestAbsolute);
        helper.assertValueEqual(chestHandlers.size(), 1, "chest handlers should have one handler");
        helper.assertTrue(chestHandlers.get(0) instanceof ItemAdapter, "chest handlers should only has ItemHandler");

        helper.assertFalse(barrelHandlers.equals(chestHandlers), "chest and barrel handlers should not have the same handler");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic", batch = "db-interact")
    public static void constructInventory(GameTestHelper helper) {
        var barrel = new BlockPos(0, 1, 0);
        var chest = new BlockPos(1, 1, 0);
        var barrelAbsolute = helper.absolutePos(barrel);
        var chestAbsolute = helper.absolutePos(chest);

        Inventory barrelInv = new Inventory(Set.of(new Position(
                helper.getLevel().dimension().location().toString(),
                barrelAbsolute.getX(), barrelAbsolute.getY(), barrelAbsolute.getZ()
        )));

        Inventory chestInv = new Inventory(Set.of(new Position(
                helper.getLevel().dimension().location().toString(),
                chestAbsolute.getX(), chestAbsolute.getY(), chestAbsolute.getZ()
        )));

        statsApi.getInventoryService().create(barrelInv);
        statsApi.getInventoryService().create(chestInv);

        helper.assertFalse(barrelInv.equals(chestInv), "barrel inventory should be equal to barrel inventory");

        helper.assertValueEqual(barrelInv.getHandlers().size(), 1, "barrel handlers should have one handler");
        helper.assertValueEqual(chestInv.getHandlers().size(), 1, "chest handlers should have one handler");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "create_vault", setupTicks = 20L)
    public static void getIItemHandlerMultiblock(GameTestHelper helper) {
        var level = helper.getLevel();

        var soloVault = new BlockPos(1, 1, 0);

        var multiblockVaultPos = List.of(
                new BlockPos(0, 1, 1),
                new BlockPos(0, 2, 1),
                new BlockPos(0, 3, 1),
                new BlockPos(1, 1, 1),
                new BlockPos(1, 2, 1),
                new BlockPos(1, 3, 1),
                new BlockPos(2, 1, 1),
                new BlockPos(2, 2, 1),
                new BlockPos(2, 3, 1)
        );

        var soloVaultAbs = helper.absolutePos(soloVault);
        helper.assertTrue(isBlock(level, soloVaultAbs, "create:item_vault"), "Expected Create vault");
        List<IHandler> soloVaultHandlers = Utils.getIHandlers(level, soloVaultAbs);
        helper.assertValueEqual(soloVaultHandlers.size(), 1, "vault handlers should have one handler");
        helper.assertTrue(soloVaultHandlers.get(0) instanceof ItemAdapter, "vault handlers should only has ItemHandler");

        List<IHandler> referenceHandlers = Utils.getIHandlers(level, helper.absolutePos(multiblockVaultPos.getFirst()));
        helper.assertValueEqual(referenceHandlers.size(), 1, "vault handlers should have one handler");
        helper.assertTrue(referenceHandlers.get(0) instanceof ItemAdapter, "vault handlers should only has ItemHandler");

        helper.assertValueEqual(referenceHandlers.size(),1, "vault should have one handler"
        );

        for (BlockPos pos : multiblockVaultPos) {
            BlockPos absPos = helper.absolutePos(pos);

            List<IHandler> handlers = Utils.getIHandlers(level, absPos);

            helper.assertValueEqual(handlers, referenceHandlers,"multiblock vault should have the same handlers at " + absPos
            );
        }

        helper.assertFalse(referenceHandlers.equals(soloVaultHandlers), "vault handlers should not have the same handlers");

        helper.succeed();
    }

    public static boolean isBlock(Level level, BlockPos pos, String blockId) {
        ResourceLocation id = BuiltInRegistries.BLOCK
                .getKey(level.getBlockState(pos).getBlock());

        return id.toString().equals(blockId);
    }

    /*
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

        var cap1 = Utils.getIHandlers(level, chest1Absolute);
        var cap2 = Utils.getIHandlers(level, chest2Absolute);

        //as the itemhandler returned by mc are not the same we test the similarity by adding a item
        ItemAdapter handler1 = ((ItemAdapter) cap1.getFirst());
        ItemAdapter handler2 = ((ItemAdapter) cap2.getFirst());

        InvWrapper i1 = (InvWrapper) handler1.getCapabilityCache().getCapability();
        InvWrapper i2 = (InvWrapper) handler1.getCapabilityCache().getCapability();

        MineStats.LOGGER.info("i1: " + i1.getInv());
        MineStats.LOGGER.info("i2: " + i2.getInv());

        MineStats.LOGGER.info("i1: " + i1.getInv().hashCode());
        MineStats.LOGGER.info("i2: " + i2.getInv().hashCode());

        helper.assertTrue(cap1.get(0) instanceof ItemAdapter, "chest handlers should only has ItemHandler");

        MineStats.LOGGER.info("handler1 = {}", handler1);
        MineStats.LOGGER.info("handler2 = {}", handler2);


        MineStats.LOGGER.info("cap1 " +  cap1 + " cap2 " + cap2);
        helper.assertValueEqual(cap1, cap2, "Handlers should be the same");
        helper.assertValueEqual(cap1.size(), 1, "chest handlers should have one handler");
        helper.assertTrue(cap1.get(0) instanceof ItemAdapter, "chest handlers should only has ItemHandler");

        helper.succeed();
    }

     */

}
