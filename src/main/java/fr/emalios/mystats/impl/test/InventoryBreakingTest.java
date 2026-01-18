package fr.emalios.mystats.impl.test;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.StatPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(MyStats.MODID)
public class InventoryBreakingTest {

    private static MineStatsTestUtils utils = MineStatsTestUtils.getInstance();

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic", batch = "db-interact", manualOnly = true)
    public static void breakSimpleInventoryTest(GameTestHelper helper) {
        //register block
        var chest = new BlockPos(1, 1, 0);
        var chestAbs = helper.absolutePos(chest);
        var player = utils.getPlayer(helper);

        InteractionResult result = utils.makePlayerRecordOn(helper, chestAbs);
        Inventory inventory = utils.buildInvFromPos(helper.getLevel(), chestAbs);

        //delete block
        helper.getLevel().destroyBlock(chestAbs, false);

        StatPlayer statPlayer = StatsAPI.getInstance().getPlayerService().getOrCreateByName(player.getName().getString());

        //assert deleted after next scan
        helper.assertTrue(statPlayer.hasInventory(inventory), "Player should have inventory");
        helper.assertTrue(StatsAPI.getInstance().getInventoryService().isLoaded(inventory), "Inventory should be loaded");
        StatsAPI.getInstance().getInventoryService().scan();

        helper.assertFalse(statPlayer.hasInventory(inventory), "Player should not have inventory");
        helper.assertFalse(StatsAPI.getInstance().getInventoryService().isLoaded(inventory), "Inventory should not be loaded");
        statPlayer = StatsAPI.getInstance().getPlayerService().getOrCreateByName(player.getName().getString());
        helper.assertFalse(statPlayer.hasInventory(inventory), "Player should not have inventory");

        helper.succeed();
    }

}
