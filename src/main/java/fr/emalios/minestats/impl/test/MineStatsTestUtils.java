package fr.emalios.minestats.impl.test;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.helper.Utils;
import fr.emalios.minestats.impl.test.snippet.RegistriesTest;
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
import java.util.stream.Collectors;

import static fr.emalios.minestats.helper.ConnectedBlocksFinder.getConnectedBlocks;

public class MineStatsTestUtils {

    private static MineStatsTestUtils INSTANCE;

    public static MineStatsTestUtils getInstance() {
        if (INSTANCE == null) INSTANCE = new MineStatsTestUtils();
        return INSTANCE;
    }

    private Player player;

    private MineStatsTestUtils() { }

    public ItemStack getRecorder(GameTestHelper helper) {
        String recorderId = MineStats.MODID+":recorder_item";
        Item recorder = RegistriesTest.loadItem(recorderId);
        helper.assertValueEqual(recorder.toString(), recorderId, "recorder item should be loaded");
        return new ItemStack(recorder);
    }

    public InteractionResult makePlayerRecordOn(GameTestHelper helper, Player player, BlockPos absolutePos) {
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
        Set<BlockPos> blockPoses = getConnectedBlocks(level, pos, Utils.getIHandlers(level, pos));
        Set<Position> positions = blockPoses.stream().map(blockPos -> new Position(
                level.dimension().location().toString(), blockPos.getX(), blockPos.getY(), blockPos.getZ()
        )).collect(Collectors.toSet());
        return new Inventory(positions);
    }

    public Player getPlayer(GameTestHelper helper) {
        if (this.player == null) this.player = helper.makeMockPlayer(GameType.SURVIVAL);
        return this.player;
    }

}
