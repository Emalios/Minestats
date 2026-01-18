package fr.emalios.mystats.helper;

import fr.emalios.mystats.MyStats;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Const {

    public static Path pathToMigrations = Path.of("src/main/resources/db/migrations");
    public static Path pathToMigrationsTest = FMLPaths.CONFIGDIR.get().resolve(MyStats.MODID + "/migrations");

    public static final String DB_FILENAME = "mystats.db";

}
