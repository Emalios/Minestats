package fr.emalios.mystats.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class RecorderItem extends Item {

    public RecorderItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        //We only want server side logic
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return InteractionResult.FAIL;

        BlockState state = level.getBlockState(pos);

        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                pos,
                state,
                be,
                null // contexte / side : null si pas nécessaire
        );
        if (handler == null) return InteractionResult.PASS;
        int slots = handler.getSlots();
        System.out.println(state.getBlock().getName() + "has an inventory with " + slots + " slots.");
        for (int i = 0; i < slots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if(stack.isEmpty()) continue;
            System.out.println(stack.getItem() + " * " + stack.getCount());
        }
        return InteractionResult.SUCCESS;
    }

}
