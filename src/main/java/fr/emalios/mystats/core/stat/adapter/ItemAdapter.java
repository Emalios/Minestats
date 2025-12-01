package fr.emalios.mystats.core.stat.adapter;

import fr.emalios.mystats.core.stat.IHandler;
import fr.emalios.mystats.core.stat.Stat;
import fr.emalios.mystats.core.stat.StatType;
import fr.emalios.mystats.core.stat.Unit;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemAdapter implements IHandler {

    private final BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache;

    public ItemAdapter(BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache) {
        this.capabilityCache = capabilityCache;
    }

    @Override
    public Collection<Stat> getContent() {
        IItemHandler inv = this.capabilityCache.getCapability();
        if(inv == null) return new ArrayList<>();

        Map<String, Stat> stacks = new HashMap<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack current = inv.getStackInSlot(i);
            if(current.isEmpty()) continue;
            Stat curStat = new Stat(StatType.ITEM, current.getItem().toString(), current.getCount(), Unit.ITEM);
            stacks.merge(current.getItem().toString(), curStat, Stat::mergeWith);
        }
        return stacks.values();
    }

    @Override
    public boolean exists() {
        return this.capabilityCache.getCapability() != null;
    }
}
