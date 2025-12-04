package fr.emalios.mystats.network;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.core.stat.Stat;
import fr.emalios.mystats.registries.StatCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record StatPayload(List<Stat> stats) implements CustomPacketPayload {

    public static final Type<StatPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyStats.MODID, "monitor_stats"));

    public static final StreamCodec<FriendlyByteBuf, List<Stat>> STAT_LIST_CODEC =
            StatCodec.STAT_STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<FriendlyByteBuf, StatPayload> STREAM_CODEC =
            StreamCodec.composite(
                    STAT_LIST_CODEC,
                    StatPayload::stats,
                    StatPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}