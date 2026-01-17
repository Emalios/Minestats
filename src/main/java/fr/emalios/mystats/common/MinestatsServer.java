package fr.emalios.mystats.common;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.Inventory;
import fr.emalios.mystats.api.models.Position;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.helper.Utils;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.impl.storage.db.Database;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.emalios.mystats.MyStats.LOGGER;

@EventBusSubscriber(modid = MyStats.MODID)
public class MinestatsServer {

    public static int counter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        if(counter < 300) return;
        counter = 0;
        StatsAPI.getInstance().getInventoryService().scan();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        //init api (in db init for now)
        StatsAPI statsAPI = StatsAPI.getInstance();
        //init storage
        Database.getInstance().init(Const.DB_FILENAME);
        LOGGER.info("[Minestats] Db loaded.");
        //load inventories to start scanning
        initInventories(event.getServer(), statsAPI);
        //StatManager.getInstance().init(event.getServer());
        LOGGER.info("[Minestats] Inventories loaded.");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        Database.getInstance().close();
        LOGGER.info("[Minestats] Db unloaded.");
        LOGGER.info("[Minestats] StatManager unloaded.");
    }

    private static void initInventories(MinecraftServer server, StatsAPI statsAPI) {
        Map<String, ServerLevel> levels = new HashMap<>();
        server.getAllLevels().forEach(level -> {
            levels.put(level.dimension().location().toString(), level);
        });
        for (Inventory inventory : statsAPI.getInventoryService().getAll()) {
            //ensure every position have the same handlers
            var allPosition = inventory.getInvPositions();
            List<IHandler> first = getHandlersFromPosition(levels, allPosition.iterator().next());
            inventory.addHandlers(first);
            for (Position invPosition : allPosition) {
                List<IHandler> handlers = getHandlersFromPosition(levels, invPosition);
                //no handlers detected on the position
                if(handlers.isEmpty() || !handlers.equals(first)) {
                    MyStats.LOGGER.warn("[Minestats] Invalid handlers for position.");
                    statsAPI.getInventoryService().removePositionFromInventory(inventory, invPosition);
                }
            }
            if(!inventory.isValid()) {
                MyStats.LOGGER.warn("[Minestats] Invalid inventory.");
                System.out.println("deleted cause invalid");
                System.out.println(inventory.getHandlers());
                System.out.println(inventory.isPersisted());
                statsAPI.getInventoryService().deleteInventory(inventory);
            }
        }
    }

    private static List<IHandler> getHandlersFromPosition(Map<String, ServerLevel> levels, Position position) {
        BlockPos pos = new BlockPos(position.getX(), position.getY(), position.getZ());
        Level level = levels.get(position.getWorld());
        //load block in world to be able to get capabilities
        level.getBlockState(pos);
        var handlers = Utils.getIHandlers(level, pos);
        MyStats.LOGGER.info("[Minestats] Found handlers [" + handlers.size() + "] for position: " + pos);
        return handlers;
    }

}
