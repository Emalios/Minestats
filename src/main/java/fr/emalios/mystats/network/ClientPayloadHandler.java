package fr.emalios.mystats.network;

import fr.emalios.mystats.content.screen.StatScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleDataOnMain(final StatPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof StatScreen screen) {
                screen.updateStats(data.stats());
            }
        });
    }
}
