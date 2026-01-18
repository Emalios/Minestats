package fr.emalios.mystats.impl.test;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.models.StatPlayer;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.McStatsAPI;
import fr.emalios.mystats.impl.storage.db.Database;
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
@GameTestHolder(MyStats.MODID)
public class DbLoadingTest {

    private static final MineStatsTestUtils utils = MineStatsTestUtils.getInstance();
    private static final StatsAPI statsApi = McStatsAPI.getInstance();

    @BeforeBatch(batch = "db-interact")
    public static void setup(ServerLevel level) {
        Database.getInstance().reset();
    }

    @AfterBatch(batch = "db-interact")
    public static void teardown(ServerLevel level) {
        MyStats.LOGGER.debug("Tearing down");
        Database.getInstance().reset();
    }

    private static void resetDb(MinecraftServer server) {
        statsApi.close();
        statsApi.init();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic", batch = "db-interact")
    public static void loadChestInventories(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var chest = new BlockPos(1, 1, 0);
        var chestAbs = helper.absolutePos(chest);

        InteractionResult result = utils.makePlayerRecordOn(helper, chestAbs);
        helper.assertTrue(result.consumesAction(), "Recorder should interact with chest");

        StatPlayer statPlayer = McStatsAPI.getInstance().getPlayerService().getOrCreateByName(player.getName().getString());
        helper.assertValueEqual(1, statPlayer.getInventories().size(), "player should have registered the inventory");
        Inventory inventory = new Inventory(Set.of(new Position(
                helper.getLevel().dimension().location().toString(),
                chestAbs.getX(), chestAbs.getY(), chestAbs.getZ()
        )));
        helper.assertTrue(statPlayer.hasInventory(inventory), "player should have inventory");

        resetDb(helper.getLevel().getServer());

        statPlayer = McStatsAPI.getInstance().getPlayerService().getOrCreateByName(player.getName().getString());
        helper.assertTrue(statPlayer.hasInventory(inventory), "player should have inventory");

        helper.succeed();
    }

}
