package fr.emalios.mystats.screen;

import fr.emalios.mystats.MyStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class LogChestMenu extends AbstractContainerMenu {

    private final LogChestBlockEntity blockEntity;
    private final Level level;

    public LogChestMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public LogChestMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.LOG_CHEST_MENU.get(), containerId);
        this.blockEntity = ((LogChestBlockEntity) blockEntity);
        this.level = inv.player.level();

        //addPlayerInventory(inv);
        //addPlayerHotbar(inv);

        int startX = 8;
        int startY = 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                int x = startX + col * 18;
                int y = startY + row * 18;
                this.addSlot(new SlotItemHandler(this.blockEntity.getInventory(), index, x, y));
            }
        }
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return false;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {

    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setRemoteSlot(int slot, ItemStack stack) {

    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return false;
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {

    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, MyStats.LOG_CHEST_BLOCK.get());
    }

    @Override
    public void slotsChanged(Container container) {

    }
}