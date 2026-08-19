package fr.emalios.minestats.common;

import com.google.common.collect.Maps;
import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.impl.storage.db.Database;
import fr.emalios.minestats.impl.storage.db.migrations.Migration;
import fr.emalios.minestats.impl.storage.db.migrations.MigrationParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MinestatsReloader extends SimplePreparableReloadListener<Map<ResourceLocation, List<String>>> {

    private static final Logger LOGGER = MineStats.LOGGER;

    private final String directory;

    public MinestatsReloader(String directory) {
        this.directory = directory;
    }

    @Override
    protected Map<ResourceLocation, List<String>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        LOGGER.info("Start scanning SQL files in {} folder.", this.directory);
        Map<ResourceLocation, List<String>> map = Maps.newHashMap();

        for(Map.Entry<ResourceLocation, Resource> entry : manager.listResources(this.directory, loc -> loc.getPath().endsWith(".sql")).entrySet()) {
            ResourceLocation loc = entry.getKey();
            try {
                BufferedReader bufferedReader = entry.getValue().openAsReader();
                map.put(loc, bufferedReader.lines().collect(Collectors.toList()));
                bufferedReader.close();
                LOGGER.info("Found {}", loc.getPath());
            } catch (IOException e) {
                LOGGER.error("Couldn't read sql file from {}\n{}", loc, e);
            }
        }
        LOGGER.info("Scanning done in {} folder.", this.directory);
        LOGGER.info("Found {} migration files in {} folder.", map.size(), this.directory);
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, List<String>> migrationsMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Database database = Database.getInstance();
        database.resetMigrations();
        for (ResourceLocation resourceLocation : migrationsMap.keySet()) {
            Migration migration = MigrationParser.parse(resourceLocation.getPath(), migrationsMap.get(resourceLocation));
            database.registerMigration(migration);
        }
    }
}
