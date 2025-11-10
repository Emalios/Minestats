package fr.emalios.mystats.content.container;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class CustomSlotItemHandler extends SlotItemHandler {

    private final Level level;
    private final boolean serverSide;

    public CustomSlotItemHandler(Level level, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.level = level;
        this.serverSide = !level.isClientSide();
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack);
        if(!stack.isEmpty() ) {
            System.out.println("SET " + stack);
        }
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        super.setByPlayer(stack);
        if(!stack.isEmpty()) {
            System.out.println("player 1 " + stack);
        }
    }

    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        System.out.println("Player 2 " + newStack + " old: " + oldStack + " current: ");
        super.setByPlayer(newStack, oldStack);
    }
}
