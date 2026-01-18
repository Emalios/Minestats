package fr.emalios.mystats.common;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.McStatsAPI;
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
        McStatsAPI.getInstance().getInventoryService().scan();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        //init api
        McStatsAPI statsAPI = McStatsAPI.getInstance();
        statsAPI.initImpl(event.getServer());
        statsAPI.init();
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
