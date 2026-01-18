package fr.emalios.mystats.impl.adapter;

import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.inventory.IHandler;
import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.record.RecordType;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FluidAdapter implements IHandler {

    private final BlockCapabilityCache<IFluidHandler, @Nullable Direction> capabilityCache;

    public FluidAdapter(BlockCapabilityCache<IFluidHandler, @Nullable Direction> capabilityCache) {
        this.capabilityCache = capabilityCache;
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
    public boolean exists() {
        return this.capabilityCache.getCapability() != null;
    }

}
