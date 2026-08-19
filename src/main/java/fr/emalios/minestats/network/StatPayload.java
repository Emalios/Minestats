package fr.emalios.minestats.network;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.models.stat.Stat;
import fr.emalios.minestats.registries.StatCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record StatPayload(List<Stat> stats) implements CustomPacketPayload {

    public static final Type<StatPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MineStats.MODID, "monitor_stats"));

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