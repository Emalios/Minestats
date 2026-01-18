package fr.emalios.mystats.common;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.storage.db.Database;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

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
        Database.getInstance().init(Const.DB_FILENAME, event.getServer());
        LOGGER.info("[Minestats] Db loaded.");
        //load inventories to start scanning
        statsAPI.getInventoryService().loadAll();
        LOGGER.info("[Minestats] Inventories loaded.");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        Database.getInstance().close();
        LOGGER.info("[Minestats] Db unloaded.");
        LOGGER.info("[Minestats] StatManager unloaded.");
    }


}
