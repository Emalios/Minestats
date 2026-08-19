package fr.emalios.minestats.registries;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.content.menu.MonitorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MineStats.MODID);

    public static final Supplier<MenuType<MonitorMenu>> MONITOR_MENU = MENUS.register("monitor_menu",
            () -> new MenuType<>(MonitorMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
