package fr.emalios.mystats.impl.test;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.impl.test.snippet.RegistriesTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class MineStatsTestUtils {

    private static MineStatsTestUtils INSTANCE;

    public static MineStatsTestUtils getInstance() {
        if (INSTANCE == null) INSTANCE = new MineStatsTestUtils();
        return INSTANCE;
    }

    private Player player;

    private MineStatsTestUtils() { }

    public ItemStack getRecorder(GameTestHelper helper) {
        String recorderId = MyStats.MODID+":recorder_item";
        Item recorder = RegistriesTest.loadItem(recorderId);
        helper.assertValueEqual(recorder.toString(), recorderId, "recorder item should be loaded");
        return new ItemStack(recorder);
    }

    public InteractionResult makePlayerRecordOn(GameTestHelper helper, BlockPos absolutePos) {
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

    public Inventory buildInvFromPos(Level level, BlockPos pos) {
        return new Inventory(Set.of(new Position(
                level.dimension().location().toString(),
                pos.getX(), pos.getY(), pos.getZ()
        )));
    }

    public Player getPlayer(GameTestHelper helper) {
        if (this.player == null) this.player = helper.makeMockPlayer(GameType.SURVIVAL);
        return this.player;
    }

}
