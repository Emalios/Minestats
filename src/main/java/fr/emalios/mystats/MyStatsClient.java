package fr.emalios.mystats;

import fr.emalios.mystats.content.screen.StatScreen;
import fr.emalios.mystats.network.OpenMonitorMenuPayload;
import fr.emalios.mystats.registries.ModMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static fr.emalios.mystats.MyStats.MODID;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class MyStatsClient {


    public static final Lazy<KeyMapping> MONITOR_MAPPING = Lazy.of(() -> new KeyMapping(
            "key." + MODID + ".monitor_mapping",
            GLFW.GLFW_KEY_O,
            "key.categories." + MODID + "." + MODID + "_category"
    ));

    public MyStatsClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.getEventBus().addListener(this::registerBindings);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.MONITOR_MENU.get(), StatScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MyStats.LOGGER.info("HELLO FROM CLIENT SETUP");
    }

    private void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(MONITOR_MAPPING.get());
    }

    @SubscribeEvent
    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (MONITOR_MAPPING.get().consumeClick()) {
            PacketDistributor.sendToServer(new OpenMonitorMenuPayload());
        }
    }
}
