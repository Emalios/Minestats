package fr.emalios.mystats.helper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static Map<String, Integer> getInventoryContent(IItemHandler inv) {
        Map<String, Integer> stacks = new HashMap<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack current = inv.getStackInSlot(i);
            if(current.isEmpty()) continue;
            stacks.merge(current.getItem().toString(), current.getCount(), Integer::sum);
        }
        return stacks;
    }

}
