package fr.emalios.minestats.network;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.content.menu.MonitorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleOpenMonitorMenu(OpenMonitorMenuPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new MonitorMenu(id, inv),
                    Component.translatable("menu." + MineStats.MODID + ".monitor")
            ));
        });
    }
}

