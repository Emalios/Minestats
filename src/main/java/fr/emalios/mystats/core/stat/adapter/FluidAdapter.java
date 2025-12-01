package fr.emalios.mystats.core.stat.adapter;

import fr.emalios.mystats.core.stat.IHandler;
import fr.emalios.mystats.core.stat.Stat;
import fr.emalios.mystats.core.stat.StatType;
import fr.emalios.mystats.core.stat.Unit;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
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
    public Collection<Stat> getContent() {
        IFluidHandler inv = this.capabilityCache.getCapability();
        if(inv == null) return new ArrayList<>();

        Map<String, Stat> stacks = new HashMap<>();
        for (int i = 0; i < inv.getTanks(); i++) {
            FluidStack current = inv.getFluidInTank(i);
            if(current.isEmpty()) continue;
            System.out.println("Current Fluid: " + current.getFluid());
            Stat curStat = new Stat(StatType.FLUID, current.getFluid().toString(), current.getAmount(), Unit.MB);
            stacks.merge(current.getFluid().toString(), curStat, Stat::mergeWith);
        }
        return stacks.values();
    }

    @Override
    public boolean exists() {
        return this.capabilityCache.getCapability() != null;
    }
}
