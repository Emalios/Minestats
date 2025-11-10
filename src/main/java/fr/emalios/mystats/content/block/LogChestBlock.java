package fr.emalios.mystats.content.block;

import fr.emalios.mystats.MyStats;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LogChestBlock extends Block implements EntityBlock {

    public LogChestBlock() {
        super(BlockBehaviour.Properties.of()
                .destroyTime(2.0f)
                .explosionResistance(10.0f)
                .sound(SoundType.WOOD)
                .strength(2.5f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogChestBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LogChestBlockEntity be) {
            player.openMenu(be, pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof LogChestBlockEntity logChestBlockEntity) {
                logChestBlockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 🔥 tick uniquement côté serveur
        if (level.isClientSide()) {
            return null;
        }

        // on ne tick que si c’est bien notre type de block entity
        return type == MyStats.LOG_CHEST_BLOCK_ENTITY.get()
                ? (lvl, pos, st, be) -> LogChestBlockEntity.tick(lvl, pos, st, (LogChestBlockEntity) be)
                : null;
    }
}