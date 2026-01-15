package fr.emalios.mystats.common;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.repository.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.sql.SQLException;
import java.util.Optional;

import static fr.emalios.mystats.MyStats.LOGGER;

@EventBusSubscriber(modid = MyStats.MODID)
public class MinestatsServer {

    private final static StatManager statManager = StatManager.getInstance();
    public static int counter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        if(counter < 300) return;
        counter = 0;
        statManager.scan();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        try {
            Database.getInstance().init(Const.DB_FILENAME);
            StatManager.getInstance().init(event.getServer());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("[Minestats] Db loaded.");
        LOGGER.info("[Minestats] StatManager loaded.");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        Database.getInstance().close();
        LOGGER.info("[Minestats] Db unloaded.");
        LOGGER.info("[Minestats] StatManager unloaded.");
    }

}
