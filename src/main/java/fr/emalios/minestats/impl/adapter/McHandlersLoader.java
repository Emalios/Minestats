package fr.emalios.minestats.impl.adapter;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.IHandler;
import fr.emalios.minestats.api.models.inventory.IHandlerLoader;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.helper.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McHandlersLoader implements IHandlerLoader {

    private MinecraftServer minecraftServer;

    public McHandlersLoader(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    @Override
    public Collection<IHandler> loadHandlers(Position position) {
        Map<String, ServerLevel> levels = new HashMap<>();
        this.minecraftServer.getAllLevels().forEach(level -> levels.put(level.dimension().location().toString(), level));
        return getHandlersFromPosition(levels, position);
    }

    private static void initInventories(MinecraftServer server, StatsAPI statsAPI) {

    }

    private static List<IHandler> getHandlersFromPosition(Map<String, ServerLevel> levels, Position position) {
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        Level level = levels.get(position.getWorld());
        //load block in world to be able to get capabilities
        level.getBlockState(pos);
        var handlers = Utils.getIHandlers(level, pos);
        MineStats.LOGGER.info("[Minestats] Found handlers [" + handlers.size() + "] for position: " + pos);
        return handlers;
    }
}
