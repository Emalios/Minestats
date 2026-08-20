package fr.emalios.minestats.impl.test;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.StatPlayer;
import fr.emalios.minestats.impl.McStatsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(MineStats.MODID)
public class InventoryBreakingTest {

    private static MineStatsTestUtils utils = MineStatsTestUtils.getInstance();
    private static final StatsAPI statsApi = McStatsAPI.getInstance();

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic", batch = "db-interact")
    public static void breakSimpleInventoryTest(GameTestHelper helper) {
        //register block
        var chest = new BlockPos(1, 1, 0);
        var chestAbs = helper.absolutePos(chest);
        var player = utils.getPlayer(helper);

        StatPlayer statPlayer = statsApi.getPlayerService().getOrCreateByName(player.getName().getString());
        MineStats.LOGGER.info("Player: " + statPlayer);

        MineStats.LOGGER.info("CC 0");
        InteractionResult result = utils.makePlayerRecordOn(helper, player, chestAbs);
        MineStats.LOGGER.info("CC 01");
        Inventory inventory = utils.buildInvFromPos(helper.getLevel(), chestAbs);
        MineStats.LOGGER.info("CC 02");
        statsApi.getInventoryService().addHandlersToInventory(inventory);

        MineStats.LOGGER.info("CC 1");


        helper.assertTrue(statPlayer.hasInventory(inventory), "Player should still have inventory before scan");
        helper.assertTrue(statsApi.getInventoryService().isLoaded(inventory), "Inventory should still be loaded before scan");
        MineStats.LOGGER.info("CC 2");

        //delete block
        helper.getLevel().destroyBlock(chestAbs, false);
        statsApi.getInventoryService().scan();
        MineStats.LOGGER.info("CC 3");

        //assert deleted after next scan
        helper.assertFalse(statsApi.getInventoryService().isLoaded(inventory), "Inventory should not be loaded");
        helper.assertFalse(statPlayer.hasInventory(inventory), "Player should not have inventory after scan");
        MineStats.LOGGER.info("CC 4");

        statPlayer = statsApi.getPlayerService().getOrCreateByName(player.getName().getString());
        helper.assertFalse(statPlayer.hasInventory(inventory), "Player should not have inventory after reloading statPlayer");
        MineStats.LOGGER.info("CC 5");

        statsApi.getInventoryService().deleteAll();
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "create_vault", batch = "db-interact", setupTicks = 10L)
    public static void breakMultiblockInventoryTest(GameTestHelper helper) {
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

        var player = utils.getPlayer(helper);

        InteractionResult result = utils.makePlayerRecordOn(helper, player, multiblockVaultPos.getFirst());
        //Inventory inventory = utils.buildInvFromPos(helper.getLevel(), multiblockVaultPos.getFirst());
        StatPlayer statPlayer = statsApi.getPlayerService().getOrCreateByName(player.getName().getString());

        Inventory inventory = statPlayer.getInventories().getFirst();

        helper.assertValueEqual(inventory.getInvPositions().size(), 9,"Builded inventory should have every positions");
        helper.assertTrue(statPlayer.hasInventory(inventory), "Player should still have inventory before scan");
        helper.assertTrue(statsApi.getInventoryService().isLoaded(inventory), "Inventory should still be loaded before scan");

        //delete block
        helper.getLevel().destroyBlock(multiblockVaultPos.getFirst(), false);

        statsApi.getInventoryService().scan();

        //assert deleted after next scan
        helper.assertFalse(statPlayer.hasInventory(inventory), "Player should not have inventory after scan");
        helper.assertFalse(statsApi.getInventoryService().isLoaded(inventory), "Inventory should not be loaded after scan");
        statPlayer = statsApi.getPlayerService().getOrCreateByName(player.getName().getString());
        helper.assertFalse(statPlayer.hasInventory(inventory), "Player should not have inventory after reloading statPlayer");

        statsApi.getInventoryService().deleteAll();
        helper.succeed();
    }

}
