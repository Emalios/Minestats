package fr.emalios.mystats.registries;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.content.menu.MonitorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MyStats.MODID);

    public static final Supplier<MenuType<MonitorMenu>> MONITOR_MENU = MENUS.register("monitor_menu",
            () -> new MenuType<>(MonitorMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
