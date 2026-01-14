package fr.emalios.mystats.impl.test.snippet;

import fr.emalios.mystats.MyStats;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Tests to know how to get items/blocks/fluids from neoforge registries
 */
@GameTestHolder(MyStats.MODID)
public class RegistriesTest {

    @GameTest(template = "empty")
    @PrefixGameTestTemplate(false)
    public static void loadItem(GameTestHelper helper) {
        String id = "minecraft:dirt";

        Item item = loadItem(id);
        helper.assertValueEqual(item.toString(), id, "item should be loaded");
        helper.succeed();
    }

    public static Item loadItem(String id) {
        ResourceLocation rl = ResourceLocation.parse(id);
        return BuiltInRegistries.ITEM.get(rl);
    }

}
