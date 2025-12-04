package fr.emalios.mystats;

import fr.emalios.mystats.command.StatCommand;
import fr.emalios.mystats.content.block.StatMonitorBlock;
import fr.emalios.mystats.content.item.RecorderItem;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.db.DatabaseSchema;
import fr.emalios.mystats.core.db.DatabaseWorker;
import fr.emalios.mystats.core.stat.StatManager;
import fr.emalios.mystats.network.ClientPayloadHandler;
import fr.emalios.mystats.network.OpenMonitorMenuPayload;
import fr.emalios.mystats.network.ServerPayloadHandler;
import fr.emalios.mystats.network.StatPayload;
import fr.emalios.mystats.registries.ModMenus;
import fr.emalios.mystats.registries.StatDataComponent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.sql.SQLException;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MyStats.MODID)
public class MyStats {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mystats";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    public static final DeferredBlock<Block> STAT_MONITOR_BLOCK = BLOCKS.registerBlock(
            "monitor_block",
            (x) -> new StatMonitorBlock()
    );

    public static final DeferredItem<BlockItem> LOG_CHEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("log_chest_block", STAT_MONITOR_BLOCK);


    // Creates a new food item with the id "mystats:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final Lazy<KeyMapping> MONITOR_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.mystats.monitor_mapping",
            GLFW.GLFW_KEY_O, // Default mouse input is the left mouse button
            "key.categories.mystats.mystats_category" // Mapping will be in the new example category
    ));

    public static final Supplier<Item> RECORDER_ITEM = ITEMS.registerItem(
            "recorder_item",
            RecorderItem::new, // The factory that the properties will be passed into.
            new RecorderItem.Properties() // The properties to use.
    );

    // Creates a creative tab with the id "mystats:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mystats")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> RECORDER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RECORDER_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MyStats(IEventBus modEventBus, ModContainer modContainer) {
        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        StatDataComponent.REGISTRAR.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MyStats) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerBindings);
        modEventBus.addListener(this::registerPayload);

        final IEventBus GAME_BUS = NeoForge.EVENT_BUS;

        GAME_BUS.addListener(this::registerCommands);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            //event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
        try {
            Database.getInstance().init();
            DatabaseSchema.createAll();
            StatManager.getInstance().init(event.getServer());
            //TODO: insert inventories into StatManager
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("[Minestats] Db loaded.");
        LOGGER.info("[Minestats] StatManager loaded.");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        StatManager.getInstance().shutdown();
        Database.getInstance().close();
        LOGGER.info("[Minestats] Db unloaded.");
        LOGGER.info("[Minestats] StatManager unloaded.");
    }

    private void registerPayload(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                StatPayload.TYPE,
                StatPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ClientPayloadHandler.handleDataOnMain(payload, context);
                    });
                }
        );
        registrar.playToServer(
                OpenMonitorMenuPayload.TYPE,
                OpenMonitorMenuPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ServerPayloadHandler.handleOpenMonitorMenu(payload, context);
                    });
                }
        );
    }

    private void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(MONITOR_MAPPING.get());
    }

    @SubscribeEvent
    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (MONITOR_MAPPING.get().consumeClick()) {
            PacketDistributor.sendToServer(new OpenMonitorMenuPayload());
        }
    }

    private void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(StatCommand.register("minestats"));
        event.getDispatcher().register(StatCommand.register("mystat"));
    }
}
