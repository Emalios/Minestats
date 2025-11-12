package fr.emalios.mystats.helper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<ItemStack> getInventoryContent(IItemHandler inv) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack current = inv.getStackInSlot(i);
            if(current.isEmpty()) continue;
            stacks.add(current);
        }
        return stacks;
    }

}
