package fr.emalios.mystats.content.container;


import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

public class SlotListener implements ContainerListener {
    @Override
    public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
        System.out.println("slot changed " + i + " " + itemStack);
    }

    @Override
    public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int i1) {
        System.out.println("data changed " + i + " " + i1);
    }
}
