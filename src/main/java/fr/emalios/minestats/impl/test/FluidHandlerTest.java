package fr.emalios.minestats.impl.test;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.IHandler;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.helper.Utils;
import fr.emalios.minestats.impl.McStatsAPI;
import fr.emalios.minestats.impl.adapter.FluidAdapter;
import fr.emalios.minestats.impl.adapter.ItemAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Test if IFluidHandlers are gotten rightly
 */
@GameTestHolder(MineStats.MODID)
public class FluidHandlerTest {

    private static MineStatsTestUtils utils = MineStatsTestUtils.getInstance();
    private static final StatsAPI statsApi = McStatsAPI.getInstance();

    @PrefixGameTestTemplate(false)
    @GameTest(template = "tank_basic")
    public static void assertTankStructure(GameTestHelper helper) {
        var tank = helper.absolutePos(new BlockPos(0, 1, 0));
        helper.assertTrue(utils.isBlock(helper.getLevel(), tank, "mekanism:ultimate_fluid_tank"), "Should be an ultimate tank");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "tank_basic")
    public static void getIFluidHandlerBasic(GameTestHelper helper) {
        var mekaTank = helper.absolutePos(new BlockPos(0, 1, 0));
        var createTank = helper.absolutePos(new BlockPos(1, 1, 0));
        var level = helper.getLevel();

        var tankBlockEntity = level.getBlockEntity(mekaTank);
        helper.assertTrue(tankBlockEntity != null, "Meka tank BlockEntity should exist");

        var createTankEntity = level.getBlockEntity(createTank);
        helper.assertTrue(createTankEntity != null, "Create tank BlockEntity should exist");

        /* Test base getIHandlers method */
        var mekaFluidHandlers = Utils.getIHandlers(level, mekaTank);
        helper.assertValueEqual(mekaFluidHandlers.size(), 2, "Meka tank should have two handlers (ont item and one fluid");

        var createFluidHandler = Utils.getIHandlers(level, createTank);
        helper.assertValueEqual(createFluidHandler.size(), 1, "create tank should have one handler");
        helper.assertTrue(createFluidHandler.get(0) instanceof FluidAdapter, "create tank should only has FluidHandler");

        helper.assertFalse(mekaFluidHandlers.contains(createFluidHandler.getFirst()), "meka tank should not contains create tank handler");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "tank_multi", setupTicks = 10L)
    public static void getIFluidHandlerMulti(GameTestHelper helper) {
        var level = helper.getLevel();
        var multiblockVaultPos = List.of(
                new BlockPos(0, 1, 0),
                new BlockPos(1, 1, 0),
                new BlockPos(0, 2, 0),
                new BlockPos(1, 2, 0),
                new BlockPos(0, 1, 1),
                new BlockPos(1, 1, 1),
                new BlockPos(1, 2, 1),
                new BlockPos(0, 2, 1)
        );

        var baseFluidHandler = Utils.getIHandlers(level, helper.absolutePos(multiblockVaultPos.getFirst()));
        MineStats.LOGGER.debug("BaseFluidHandler: {} gotten at: {}", baseFluidHandler, helper.absolutePos(multiblockVaultPos.getFirst()));
        for (BlockPos multiblockVaultPo : multiblockVaultPos) {
            var multiblockVaultAbsPos = helper.absolutePos(multiblockVaultPo);
            helper.assertTrue(utils.isBlock(level, multiblockVaultAbsPos, "create:fluid_tank"), "Expected Create tank at " + multiblockVaultPo);

            var tankBlockEntity = level.getBlockEntity(multiblockVaultAbsPos);
            helper.assertTrue(tankBlockEntity != null, "Tank BlockEntity should exist at " + multiblockVaultPo);

            var fluidHandler = Utils.getIHandlers(level, multiblockVaultAbsPos);
            helper.assertValueEqual(fluidHandler.size(), 1, "create tank should have one handler");
            helper.assertTrue(fluidHandler.get(0) instanceof FluidAdapter, "create tank should only has FluidHandler");
            MineStats.LOGGER.debug("Current Fluid Handler: {} gottent at: {}", fluidHandler, multiblockVaultAbsPos);
            helper.assertValueEqual(fluidHandler, baseFluidHandler, "All handlers on the multi tank block should be the same");
        }

        helper.succeed();
    }

}
