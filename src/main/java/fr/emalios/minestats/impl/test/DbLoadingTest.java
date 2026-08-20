package fr.emalios.minestats.impl.test;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.api.models.StatPlayer;
import fr.emalios.minestats.impl.McStatsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

/**
 * Class to test the persistency of the data stored in the database, especially the loading logic for handlers in example
 * as all the logics is already made in JUnit tests
 */
@GameTestHolder(MineStats.MODID)
public class DbLoadingTest {

    private static final MineStatsTestUtils utils = MineStatsTestUtils.getInstance();
    private static final StatsAPI statsApi = McStatsAPI.getInstance();

    @BeforeBatch(batch = "db-interact")
    public static void setup(ServerLevel level) {
        MineStats.LOGGER.debug("[SETUP-TEST] Remove all inventories");
        statsApi.getInventoryService().deleteAll();
    }

    @AfterBatch(batch = "db-interact")
    public static void teardown(ServerLevel level) {
        MineStats.LOGGER.debug("[TEARING DOWN-TEST] Remove all inventories");
        statsApi.getInventoryService().deleteAll();
    }

    private static void restartDb(MinecraftServer server) {
        MineStats.LOGGER.debug("Reset close db");
        statsApi.close();
        statsApi.init();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic", batch = "db-interact")
    public static void loadChestInventories(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var chest = new BlockPos(1, 1, 0);
        var chestAbs = helper.absolutePos(chest);

        InteractionResult result = utils.makePlayerRecordOn(helper, player, chestAbs);
        helper.assertTrue(result.consumesAction(), "Recorder should interact with chest");

        StatPlayer statPlayer = statsApi.getPlayerService().getOrCreateByName(player.getName().getString());

        helper.assertValueEqual(1, statPlayer.getInventories().size(), "player should have registered the inventory");
        Inventory inventory = new Inventory(Set.of(new Position(
                helper.getLevel().dimension().location().toString(),
                chestAbs.getX(), chestAbs.getY(), chestAbs.getZ()
        )));
        statsApi.getInventoryService().addHandlersToInventory(inventory);
        helper.assertTrue(statPlayer.hasInventory(inventory), "player should have inventory");
        //fake reload of the world to test persistency
        restartDb(helper.getLevel().getServer());

        statPlayer = statsApi.getPlayerService().getOrCreateByName(player.getName().getString());
        helper.assertTrue(statPlayer.hasInventory(inventory), "player should have inventory");

        statsApi.getInventoryService().deleteAll();
        helper.succeed();
    }

}
