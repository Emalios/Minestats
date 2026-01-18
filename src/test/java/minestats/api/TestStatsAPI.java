package minestats.api;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.IHandlerLoader;
import fr.emalios.mystats.api.storage.*;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.mystats.impl.storage.repository.*;
import minestats.api.storage.TestHandlerLoader;
import org.slf4j.LoggerFactory;

import static minestats.utils.Const.pathToMigrations;

public class TestStatsAPI extends StatsAPI {

    private static TestStatsAPI instance;
    public static TestStatsAPI getInstance() {
        if (instance == null) {
            instance = new TestStatsAPI();
        }
        return instance;
    }

    private final Database database;

    private TestStatsAPI() {
        database = new Database();
    }

    @Override
    public void init() {
        new MigrationLoader(pathToMigrations).loadAll().forEach(database::registerMigration);
        super.init();
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
        return new TestHandlerLoader();
    }

    @Override
    public void onInit() {
        this.database.init(":memory:", LoggerFactory.getLogger(TestStatsAPI.class));
    }

    @Override
    public void onShutdown() {
        this.database.close();
    }
}
