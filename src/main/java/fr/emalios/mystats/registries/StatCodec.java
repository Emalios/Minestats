package fr.emalios.mystats.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.emalios.mystats.content.item.RecorderDataComponent;
import fr.emalios.mystats.content.item.RecorderDataComponent.RecorderData;
import fr.emalios.mystats.api.CountUnit;
import fr.emalios.mystats.api.RecordType;
import fr.emalios.mystats.api.stat.Stat;
import fr.emalios.mystats.api.stat.TimeUnit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class StatCodec {

    private static <E extends Enum<E>> StreamCodec<FriendlyByteBuf, E> enumCodec(Class<E> clazz) {
        // use of(...) to implement enum read/write; composite accepts StreamCodec<? super B, T>
        return StreamCodec.of(
                FriendlyByteBuf::writeEnum,
                buf -> buf.readEnum(clazz)
        );
    }

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

    public static final StreamCodec<FriendlyByteBuf, Stat> STAT_STREAM_CODEC =
            StreamCodec.composite(
                    // 1st field: RecordType (enum)
                    enumCodec(RecordType.class), Stat::getType,
                    // 2nd field: resourceId (string). ByteBufCodecs.STRING_UTF8 is StreamCodec<ByteBuf,String>,
                    // which is compatible because composite accepts StreamCodec<? super B, T>.
                    ByteBufCodecs.STRING_UTF8, Stat::getResourceId,
                    // 3rd field: count (float)
                    ByteBufCodecs.FLOAT, Stat::getCount,
                    // 4th field: CountUnit (enum)
                    enumCodec(CountUnit.class), Stat::getUnit,
                    // 5th field: TimeUnit (enum)
                    enumCodec(TimeUnit.class), Stat::getTimeUnit,
                    // final: constructor function (Function5)
                    Stat::new
            );
}
