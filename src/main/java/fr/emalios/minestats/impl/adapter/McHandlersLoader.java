package fr.emalios.minestats.impl.adapter;

import fr.emalios.minestats.api.models.inventory.IHandler;
import fr.emalios.minestats.api.models.inventory.IHandlerLoader;
import fr.emalios.minestats.api.models.inventory.IPosition;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.helper.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class McHandlersLoader implements IHandlerLoader {

    private final Map<String, ServerLevel> levels = new HashMap<>();

    public McHandlersLoader(MinecraftServer minecraftServer) {
        minecraftServer.getAllLevels().forEach(level -> this.levels.put(level.dimension().location().toString(), level));
    }

    @Override
    public Collection<IHandler> loadHandlers(IPosition position) {
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        Level level = this.levels.get(position.getWorld());
        //load block in world to be able to get capabilities
        level.getBlockState(pos);
        return Utils.getIHandlers(level, pos);
    }

}
