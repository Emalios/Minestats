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
import java.util.stream.Collectors;

import static fr.emalios.minestats.helper.ConnectedBlocksFinder.getConnectedBlocks;

/**
 * Test the construction of an inventory which could be a multiblock if connecting blocks have the same handlers
 */
@GameTestHolder(MineStats.MODID)
public class InventoryConstructionTest {

    private static final MineStatsTestUtils utils = MineStatsTestUtils.getInstance();
    private static final StatsAPI statsApi = McStatsAPI.getInstance();

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic")
    public static void constructSingleChestInventory(GameTestHelper helper) {
        var level = helper.getLevel();

        var chestPos = new BlockPos(1, 1, 0);
        var chestAbsPos = helper.absolutePos(chestPos);

        Inventory soloVaultInv = utils.buildInvFromPos(level, chestAbsPos);
        statsApi.getInventoryService().addHandlersToInventory(soloVaultInv);

        helper.assertValueEqual(soloVaultInv.getHandlers().size(), 1, "Inventory should only has one handler");
        helper.assertValueEqual(soloVaultInv.getInvPositions().size(), 1, "Inventory should only has one position");
        helper.assertTrue(soloVaultInv.containsPosition(new Position(level.dimension().location().toString(), chestAbsPos.getX(), chestAbsPos.getY(), chestAbsPos.getZ())), "Inventory should contain position");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "create_vault")
    public static void constructSingleVaultInventory(GameTestHelper helper) {
        var level = helper.getLevel();

        var soloVault = new BlockPos(1, 1, 0);
        var soloVaultAbs = helper.absolutePos(soloVault);

        Inventory soloVaultInv = utils.buildInvFromPos(level, soloVaultAbs);
        statsApi.getInventoryService().addHandlersToInventory(soloVaultInv);

        helper.assertValueEqual(soloVaultInv.getHandlers().size(), 1, "Inventory should only has one handler");
        helper.assertValueEqual(soloVaultInv.getInvPositions().size(), 1, "Inventory should only has one position");
        helper.assertTrue(soloVaultInv.containsPosition(new Position(level.dimension().location().toString(), soloVaultAbs.getX(), soloVaultAbs.getY(), soloVaultAbs.getZ())), "Inventory should contain position");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "tank_basic")
    public static void constructBasicTankInventory(GameTestHelper helper) {
        var level = helper.getLevel();
        var mekaTank = helper.absolutePos(new BlockPos(0, 1, 0));
        var createTank = helper.absolutePos(new BlockPos(1, 1, 0));

        Inventory mekaTankInv = utils.buildInvFromPos(level, mekaTank);
        statsApi.getInventoryService().addHandlersToInventory(mekaTankInv);

        Inventory createTankInv = utils.buildInvFromPos(level, createTank);
        statsApi.getInventoryService().addHandlersToInventory(createTankInv);

        helper.assertValueEqual(mekaTankInv.getHandlers().size(), 2, "Meka tank Inventory should only has one handler");
        helper.assertValueEqual(mekaTankInv.getInvPositions().size(), 1, "Meka tank Inventory should only has one position");
        helper.assertTrue(mekaTankInv.containsPosition(new Position(level.dimension().location().toString(), mekaTank.getX(), mekaTank.getY(), mekaTank.getZ())), "Inventory should contain position");

        helper.assertValueEqual(createTankInv.getHandlers().size(), 1, "Create tank Inventory should only has one handler");
        helper.assertValueEqual(createTankInv.getInvPositions().size(), 1, "Create tank Inventory should only has one position");
        helper.assertTrue(createTankInv.containsPosition(new Position(level.dimension().location().toString(), createTank.getX(), createTank.getY(), createTank.getZ())), "Inventory should contain position");

        helper.assertFalse(mekaTankInv.equals(createTankInv), "tanks inventories should not be the same");

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "create_vault", setupTicks = 10L)
    public static void constructMultiItemInventory(GameTestHelper helper) {
        var level = helper.getLevel();

        var multiblockVaultPos = List.of(
                helper.absolutePos(new BlockPos(0, 1, 1)),
                helper.absolutePos(new BlockPos(0, 2, 1)),
                helper.absolutePos(new BlockPos(0, 3, 1)),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                helper.absolutePos(new BlockPos(1, 2, 1)),
                helper.absolutePos(new BlockPos(1, 3, 1)),
                helper.absolutePos(new BlockPos(2, 1, 1)),
                helper.absolutePos(new BlockPos(2, 2, 1)),
                helper.absolutePos(new BlockPos(2, 3, 1))
        );
        //init the first inv to compare
        Inventory firstInv = utils.buildInvFromPos(level, multiblockVaultPos.getFirst());
        statsApi.getInventoryService().addHandlersToInventory(firstInv);

        //test from every single vault block
        for (BlockPos basePos : multiblockVaultPos) {
            Inventory multiblockVaultInv = utils.buildInvFromPos(level, basePos);
            statsApi.getInventoryService().addHandlersToInventory(multiblockVaultInv);

            helper.assertValueEqual(multiblockVaultInv.getHandlers().size(), 1, "Inventory should only has one handler");
            helper.assertValueEqual(multiblockVaultInv.getInvPositions().size(), 9, "Inventory should only has nine positions");
            helper.assertValueEqual(multiblockVaultInv, firstInv, "All inventory should be the same");
            for (BlockPos multiblockVaultPo : multiblockVaultPos) {
                helper.assertTrue(multiblockVaultInv.containsPosition(new Position(level.dimension().location().toString(), multiblockVaultPo.getX(), multiblockVaultPo.getY(), multiblockVaultPo.getZ())), "Inventory should contain position");
            }
        }

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "tank_multi", setupTicks = 10L)
    public static void constructMultiTankInventory(GameTestHelper helper) {
        var level = helper.getLevel();

        var multiblockVaultPos = List.of(
                helper.absolutePos(new BlockPos(0, 1, 0)),
                helper.absolutePos(new BlockPos(1, 1, 0)),
                helper.absolutePos(new BlockPos(0, 2, 0)),
                helper.absolutePos(new BlockPos(1, 2, 0)),
                helper.absolutePos(new BlockPos(0, 1, 1)),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                helper.absolutePos(new BlockPos(1, 2, 1)),
                helper.absolutePos(new BlockPos(0, 2, 1))
        );
        //init the first inv to compare
        Inventory multiTankInv = utils.buildInvFromPos(level, multiblockVaultPos.getFirst());
        statsApi.getInventoryService().addHandlersToInventory(multiTankInv);

        //test from every single vault block
        for (BlockPos basePos : multiblockVaultPos) {
            Inventory multiblockVaultInv = utils.buildInvFromPos(level, basePos);
            statsApi.getInventoryService().addHandlersToInventory(multiblockVaultInv);

            helper.assertValueEqual(multiblockVaultInv.getHandlers().size(), 1, "Create tank Inventory should only has one handler");
            helper.assertValueEqual(multiblockVaultInv.getInvPositions().size(), 8, "Create tank Inventory should only has nine positions");
            helper.assertValueEqual(multiblockVaultInv, multiTankInv, "All create tank inventory should be the same");
            for (BlockPos multiblockVaultPo : multiblockVaultPos) {
                helper.assertTrue(multiblockVaultInv.containsPosition(new Position(level.dimension().location().toString(), multiblockVaultPo.getX(), multiblockVaultPo.getY(), multiblockVaultPo.getZ())), "Inventory should contain position");
            }
        }

        helper.succeed();
    }

}
