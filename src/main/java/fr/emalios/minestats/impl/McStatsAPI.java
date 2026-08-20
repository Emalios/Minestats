package fr.emalios.minestats.impl;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.IHandlerLoader;
import fr.emalios.minestats.api.storage.*;
import fr.emalios.minestats.helper.Const;
import fr.emalios.minestats.impl.adapter.McHandlersLoader;
import fr.emalios.minestats.impl.storage.db.Database;
import fr.emalios.minestats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.minestats.impl.storage.repository.*;
import net.minecraft.server.MinecraftServer;

import java.sql.SQLException;

import static fr.emalios.minestats.MineStats.LOGGER;

public class McStatsAPI extends StatsAPI {

    private static McStatsAPI instance;

    public static McStatsAPI getInstance() {
        if (instance == null) {
            instance = new McStatsAPI();
        }
        return instance;
    }

    private Database database;
    private IHandlerLoader handlerLoader;

    private McStatsAPI() {}

    public void initImpl(MinecraftServer server) {
        this.handlerLoader = new McHandlersLoader(server);
        this.database = new Database();
    }

    @Override
    public PlayerRepository buildPlayerRepository() {
        return new SqlitePlayerRepository(this.database.getPlayerDao());
    }

    @Override
    public PlayerInventoryRepository buildPlayerInventoryRepository() {
        return new SqlitePlayerInventoryRepository(this.database.getPlayerInventoryDao());
    }

    @Override
    public InventorySnapshotRepository buildInventorySnapshotRepository() {
        return new SqliteInventorySnapshotRepository(this.database.getInventorySnapshotDao(), this.database.getRecordDao());
    }

    @Override
    public InventoryPositionsRepository buildInventoryPositionsRepository() {
        return new SqliteInventoryPositionsRepository(this.database.getInventoryPositionsDao());
    }

    @Override
    public InventoryRepository buildInventoryRepository() {
        return new SqliteInventoryRepository(this.database.getInventoryDao(), this.database.getInventoryPositionsDao());
    }

    @Override
    public IHandlerLoader getIHandlerLoader() {
        return this.handlerLoader;
    }

    @Override
    public void onInit() {
        //load migrations
        new MigrationLoader(Const.MIGRATIONS_STRING_PATH).loadAll().forEach(database::registerMigration);
        this.database.init(Const.DB_FILENAME, LOGGER);
        LOGGER.info("[Minestats] Db loaded.");
    }

    @Override
    public void onShutdown() {
        this.database.close();
    }

}
