package fr.emalios.minestats.helper;

import fr.emalios.minestats.MineStats;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Const {

    public static String MIGRATIONS_STRING_PATH = "data/minestats/migration/";
    public static Path pathToMigrationsTest = FMLPaths.CONFIGDIR.get().resolve(MineStats.MODID + "/migrations");

    public static final String DB_FILENAME = "minestats.db";

}
