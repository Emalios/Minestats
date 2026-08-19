package fr.emalios.minestats.registries;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.content.item.RecorderDataComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static fr.emalios.minestats.registries.StatCodec.*;

public class StatDataComponent {

    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MineStats.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RecorderDataComponent.RecorderData>> RECORDER_COMPONENT = REGISTRAR.registerComponentType(
            "recorder_data",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(RECORDER_DATA_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(RECORDER_DATA_STREAM_CODEC)
    );

}
