package fr.emalios.minestats.impl.adapter;

import fr.emalios.minestats.api.models.record.CountUnit;
import fr.emalios.minestats.api.models.inventory.IHandler;
import fr.emalios.minestats.api.models.record.Record;
import fr.emalios.minestats.api.models.record.RecordType;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FluidAdapter implements IHandler {

    private final BlockCapabilityCache<IFluidHandler, @Nullable Direction> capabilityCache;
    private final IFluidHandler cachedFluidHandler;

    public FluidAdapter(BlockCapabilityCache<IFluidHandler, @Nullable Direction> capabilityCache) {
        this.capabilityCache = capabilityCache;
        this.cachedFluidHandler = capabilityCache.getCapability();
    }

    @Override
    public Collection<Record> getContent() {
        IFluidHandler inv = this.capabilityCache.getCapability();
        if(inv == null) return new ArrayList<>();

        Map<String, Record> stacks = new HashMap<>();
        for (int i = 0; i < inv.getTanks(); i++) {
            FluidStack current = inv.getFluidInTank(i);
            if(current.isEmpty()) continue;
            Record curRecord = new Record(RecordType.FLUID, current.getFluid().toString(), current.getAmount(), CountUnit.MB);
            stacks.merge(current.getFluid().toString(), curRecord, Record::mergeWith);
        }
        return stacks.values();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FluidAdapter that = (FluidAdapter) o;
        return Objects.equals(capabilityCache.getCapability(), that.capabilityCache.getCapability());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                capabilityCache.getCapability()
        );
    }

    @Override
    public boolean hasChanged() {
        if(this.capabilityCache.getCapability() == null) return true;
        return !Objects.equals(this.capabilityCache.getCapability(), this.cachedFluidHandler);
    }

    @Override
    public boolean exists() {
        return this.capabilityCache.getCapability() != null;
    }

}
