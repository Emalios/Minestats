package fr.emalios.mystats.network;

import fr.emalios.mystats.MyStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenMonitorMenuPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenMonitorMenuPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MyStats.MODID, "open_monitor_menu"));

    public static final StreamCodec<FriendlyByteBuf, OpenMonitorMenuPayload> STREAM_CODEC =
                StreamCodec.of((buf, payload) -> {}, buf -> new OpenMonitorMenuPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
}

