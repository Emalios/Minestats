package fr.emalios.mystats.event;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.repository.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.sql.SQLException;

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
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
        try {
            Database.getInstance().init();
            var playerInvRepo = new SqlitePlayerInventoryRepository(Database.getInstance().getPlayerInventoryDao());
            Storage.register(
                    new SqlitePlayerRepository(Database.getInstance().getPlayerDao(), playerInvRepo),
                    playerInvRepo,
                    new SqliteInventoryRepository(Database.getInstance().getInventoryDao(), Database.getInstance().getInventoryPositionsDao()),
                    new SqliteInventorySnapshotRepository(
                            Database.getInstance().getInventorySnapshotDao(),
                            Database.getInstance().getRecordDao()),
                    new SqliteInventoryPositionsRepository(Database.getInstance().getInventoryPositionsDao())
            );
            StatManager.getInstance().init(event.getServer());
            //TODO: insert inventories into StatManager
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("[Minestats] Db loaded.");
        LOGGER.info("[Minestats] StatManager loaded.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Database.getInstance().close();
        LOGGER.info("[Minestats] Db unloaded.");
        LOGGER.info("[Minestats] StatManager unloaded.");
    }

}
