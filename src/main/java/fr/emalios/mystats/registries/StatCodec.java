package fr.emalios.mystats.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.emalios.mystats.content.item.RecorderDataComponent;
import fr.emalios.mystats.content.item.RecorderDataComponent.RecorderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class StatCodec {

    public static final Codec<RecorderData> RECORDER_DATA_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING
                            .xmap(
                                    RecorderDataComponent.RecorderMode::valueOf, // String -> Enum
                                    Enum::name // Enum -> String
                            )
                            .fieldOf("mode")
                            .forGetter(RecorderDataComponent.RecorderData::mode)
            ).apply(instance, RecorderDataComponent.RecorderData::new));

    // --- STREAM_CODEC pour la synchronisation réseau
    public static final StreamCodec<ByteBuf, RecorderDataComponent.RecorderData> RECORDER_DATA_STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(
                            RecorderDataComponent.RecorderMode::valueOf,
                            Enum::name
                    ),
                    RecorderDataComponent.RecorderData::mode,
                    RecorderDataComponent.RecorderData::new
            );

}
