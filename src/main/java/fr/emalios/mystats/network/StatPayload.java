package fr.emalios.mystats.network;

import fr.emalios.mystats.MyStats;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record StatPayload(Map<String, Double> stats) implements CustomPacketPayload {


    public static final CustomPacketPayload.Type<StatPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MyStats.MODID, "monitor_stats"));

    public static final StreamCodec<FriendlyByteBuf, StatPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeInt(payload.stats().size());
                        payload.stats().forEach((key, value) -> {
                            buf.writeUtf(key);
                            buf.writeDouble(value);
                        });
                    },
                    buf -> {
                        int size = buf.readInt();
                        Map<String, Double> map = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            map.put(buf.readUtf(), buf.readDouble());
                        }
                        return new StatPayload(map);
                    }
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
