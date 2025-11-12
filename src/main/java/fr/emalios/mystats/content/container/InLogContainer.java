package fr.emalios.mystats.content.container;

import fr.emalios.mystats.core.stat.Stat;
import fr.emalios.mystats.core.stat.StatManager;
import fr.emalios.mystats.core.stat.StatType;
import fr.emalios.mystats.core.stat.Unit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.time.Instant;
import java.util.UUID;

public class InLogContainer extends ItemStackHandler {

    private final BlockEntity owner;
    private Level level;
    private String pos;

    public InLogContainer(int size, BlockEntity owner) {
        super(size);
        this.owner = owner;
        this.pos = owner.getBlockPos().toShortString();
    }

    public InLogContainer(BlockEntity owner) {
        super(27);
        this.owner = owner;
        this.pos = owner.getBlockPos().toShortString();
    }


    private Level getLevel() {
        if (this.level == null) {
            this.level = owner.getLevel();
        }
        return this.level;
    }

    private boolean isServerSide() {
        Level lvl = this.getLevel();
        return lvl != null && !lvl.isClientSide();
    }


    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if(stack.isEmpty()) return;
        super.setStackInSlot(slot, stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if(!this.isServerSide()) return;
        this.owner.setChanged();
        this.getLevel().sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), 3);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack item = super.insertItem(slot, stack, simulate);
        //avoid logging simulate
        if(simulate) return item;
        //if item is empty every stack went inside the container
        if(item.isEmpty()) {
        } else {
            int count = stack.getCount() - item.getCount();
        }
        return item;
    }
}
