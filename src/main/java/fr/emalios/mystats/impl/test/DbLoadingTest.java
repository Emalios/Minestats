package fr.emalios.mystats.impl.test;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Position;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.test.snippet.RegistriesTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

/**
 * Class to test the persistency of the data stored in the database, especially the loading logic for handlers in example
 * as all the logics is already made in JUnit tests
 */
@GameTestHolder(MyStats.MODID)
public class DbLoadingTest {

    private static Player player;

    @BeforeBatch(batch = "db-interact")
    public static void setup(ServerLevel level) {
    }

    @AfterBatch(batch = "db-interact")
    public static void teardown(ServerLevel level) {
        StatManager.getInstance().reset();
        Database.getInstance().reset();
    }

    private static Player getPlayer(GameTestHelper helper) {
        if(player == null) {
            player = helper.makeMockPlayer(GameType.SURVIVAL);
        }
        return player;
    }

    private static ItemStack getRecorder(GameTestHelper helper) {
        String recorderId = MyStats.MODID+":recorder_item";
        Item recorder = RegistriesTest.loadItem(recorderId);
        helper.assertValueEqual(recorder.toString(), recorderId, "recorder item should be loaded");
        return new ItemStack(recorder);
    }

    private static InteractionResult makePlayerRecordOn(GameTestHelper helper, BlockPos absolutePos) {
        var player = getPlayer(helper);

        ItemStack recorder = getRecorder(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, recorder);
        helper.assertValueEqual(recorder, player.getItemInHand(InteractionHand.MAIN_HAND), "item in hand should be recorder item");

        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolutePos),
                Direction.UP,
                absolutePos,
                false
        );
        return recorder.useOn(
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit)
        );
    }

    private static void resetDb() {
        Database.getInstance().close();
        Database.getInstance().init(Const.DB_FILENAME);
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "chest_basic", batch = "db-interact", manualOnly = true)
    public static void loadChestInventories(GameTestHelper helper) {
        var chest = new BlockPos(1, 1, 0);
        var chestAbs = helper.absolutePos(chest);

        InteractionResult result = makePlayerRecordOn(helper, chestAbs);
        helper.assertTrue(result.consumesAction(), "Recorder should interact with chest");

        StatPlayer statPlayer = Storage.players().getOrCreate(player.getName().getString());
        helper.assertValueEqual(1, statPlayer.getInventories().size(), "player should have registered the inventory");
        Inventory inventory = new Inventory(Set.of(new Position(
                helper.getLevel().dimension().location().toString(),
                chestAbs.getX(), chestAbs.getY(), chestAbs.getZ()
        )));
        helper.assertTrue(statPlayer.hasInventory(inventory), "player should have inventory");

        resetDb();

        statPlayer = Storage.players().getOrCreate(player.getName().getString());
        helper.assertTrue(statPlayer.hasInventory(inventory), "player should have inventory");

        helper.succeed();
    }

}
