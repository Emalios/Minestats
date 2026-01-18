package fr.emalios.mystats.impl;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.IHandlerLoader;
import fr.emalios.mystats.api.storage.*;
import fr.emalios.mystats.helper.Const;
import fr.emalios.mystats.impl.adapter.McHandlersLoader;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.repository.*;
import net.minecraft.server.MinecraftServer;

import static fr.emalios.mystats.MyStats.LOGGER;

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
        this.database.init(Const.DB_FILENAME, LOGGER);
        LOGGER.info("[Minestats] Db loaded.");
    }

    @Override
    public void onShutdown() {
        this.database.close();
    }
}
