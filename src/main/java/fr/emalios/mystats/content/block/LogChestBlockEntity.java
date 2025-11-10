package fr.emalios.mystats.content.block;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.content.container.InLogContainer;
import fr.emalios.mystats.screen.LogChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class LogChestBlockEntity extends BlockEntity implements MenuProvider {

    private final InLogContainer inventory;
    private final Lazy<IItemHandler> itemHandler;
    private int tickCounter = 0;


    public LogChestBlockEntity(BlockPos pos, BlockState state) {
        super(MyStats.LOG_CHEST_BLOCK_ENTITY.get(), pos, state);
        this.inventory = new InLogContainer(27, this);
        this.itemHandler = Lazy.of(() -> inventory);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public IItemHandler getItemHandler() {
        return itemHandler.get();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Log Chest");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player player) {
        return new LogChestMenu(i, playerInventory, this);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LogChestBlockEntity be) {
        if (level.isClientSide()) return;

         be.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        tickCounter++;

        if (tickCounter % 20 == 0) {
            // tu peux vérifier ton inventaire ici
            // ou faire un flush vers ta base de données, etc.
            // => évite les accès disques ou NBT trop fréquents
            // => pense batch
        }
    }

}