package fr.emalios.mystats.event;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.core.stat.StatManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.sql.SQLException;

@EventBusSubscriber(modid = MyStats.MODID)
public class CustomServerTickEvent {

    private final static StatManager statManager = StatManager.getInstance();
    public static int counter = 0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        counter++;
        if(counter < 300) return;
        counter = 0;
        System.out.println("SCANNING");
        try {
            statManager.scan();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
