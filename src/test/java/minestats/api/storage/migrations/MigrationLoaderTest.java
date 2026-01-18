package minestats.api.storage.migrations;

import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static minestats.utils.Const.pathToMigrations;

@DisplayName("Migration's tests")
public class MigrationLoaderTest {

    private static int NUMBER_OF_MIGRATIONS = 2;

    @Test
    @DisplayName("Get migration test")
    public void getMigrationTest() {
        MigrationLoader migrationLoader = new MigrationLoader(pathToMigrations);
        var migrations = migrationLoader.loadAll();
        Assertions.assertNotNull(migrations);
        Assertions.assertFalse(migrations.isEmpty());
        Assertions.assertEquals(NUMBER_OF_MIGRATIONS, migrations.size());

        for (int i = 0; i < migrations.size(); i++) {
            var migration = migrations.get(i);
            Assertions.assertEquals(i+1, migration.version());
        }
    }

    @Test
    @DisplayName("Load migrations multiple times into database")
    public void loadMultipleMigrationsTest() {
        Database database = new Database();
        new MigrationLoader(pathToMigrations).loadAll().forEach(database::registerMigration);
        new MigrationLoader(pathToMigrations).loadAll().forEach(database::registerMigration);
        var migrations = database.getMigrations();
        Assertions.assertNotNull(database.getMigrations());
        Assertions.assertEquals(NUMBER_OF_MIGRATIONS, database.getMigrations().size());

        //assert order
        for (int i = 0; i < database.getMigrations().size(); i++) {
            var migration = migrations.get(i);
            Assertions.assertEquals(i+1, migration.version());
        }
    }

}
