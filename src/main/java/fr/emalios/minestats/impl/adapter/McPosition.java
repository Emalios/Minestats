package fr.emalios.minestats.impl.adapter;


import fr.emalios.minestats.api.models.inventory.IPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class McPosition extends BlockPos implements IPosition {

    private static String getWorldName(Level level) {
        return level.dimension().location().toString();
    }

    private final String world;

    public McPosition(int x, int y, int z, String world) {
        super(x, y, z);
        this.world = world;
    }

    public McPosition(BlockPos pos, Level level) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.world = getWorldName(level);
    }

    private McPosition(BlockPos pos, String world) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.world = world;
    }

    public McPosition(IPosition pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.world = pos.getWorld();
    }

    @Override
    public String getWorld() {
        return this.world;
    }

    @Override
    public Collection<IPosition> getAdjacentPositions() {
        return Stream.of(
                super.north(), super.south(),
                super.east(), super.west(),
                super.above(), super.below()
        ).map(blockPos -> new McPosition(blockPos, this.world)).collect(Collectors.toSet());
    }
}
