package fr.emalios.mystats.content.block;

import fr.emalios.mystats.MyStats;
import net.minecraft.core.BlockPos;
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

public class StatMonitorBlock extends Block {

    public StatMonitorBlock() {
        super(BlockBehaviour.Properties.of()
                .destroyTime(2.0f)
                .explosionResistance(10.0f)
                .sound(SoundType.WOOD)
                .strength(2.5f));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide()) return InteractionResult.SUCCESS;
        //open inventory

        return InteractionResult.SUCCESS;
    }
}