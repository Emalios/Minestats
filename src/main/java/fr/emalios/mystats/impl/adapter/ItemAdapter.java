package fr.emalios.mystats.impl.adapter;

import fr.emalios.mystats.api.stat.CountUnit;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.stat.Record;
import fr.emalios.mystats.api.stat.RecordType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ItemAdapter implements IHandler {

    private final BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache;

    public ItemAdapter(BlockCapabilityCache<IItemHandler, @Nullable Direction> capabilityCache) {
        this.capabilityCache = capabilityCache;
    }

    @Override
    public Collection<Record> getContent() {
        IItemHandler inv = this.capabilityCache.getCapability();
        if(inv == null) return new ArrayList<>();

        Map<String, Record> stacks = new HashMap<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack current = inv.getStackInSlot(i);
            if(current.isEmpty()) continue;
            Record curRecord = new Record(RecordType.ITEM, current.getItem().toString(), current.getCount(), CountUnit.ITEM);
            stacks.merge(current.getItem().toString(), curRecord, Record::mergeWith);
        }
        return stacks.values();
    }

    @Override
    public boolean exists() {
        return this.capabilityCache.getCapability() != null;
    }

    @Override
    public void save() {

    }

    @Override
    public void delete() {

    }
}
