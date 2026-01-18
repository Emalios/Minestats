package fr.emalios.mystats.impl.adapter;

import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.inventory.IHandler;
import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.record.RecordType;
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ItemAdapter that = (ItemAdapter) o;
        return Objects.equals(capabilityCache.getCapability(), that.capabilityCache.getCapability());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(capabilityCache.getCapability());
    }

    @Override
    public boolean exists() {
        return this.capabilityCache.getCapability() != null;
    }

    @Override
    public String toString() {
        return this.capabilityCache.getCapability().toString();
    }
}
