package minestats.api.storage.migrations;

import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;
import minestats.api.storage.DatabaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

import static minestats.utils.Const.pathToMigrations;

@DisplayName("Migration's tests")
public class MigrationLoaderTest {

    @Test
    @DisplayName("Get migration test")
    public void getMigrationTest() throws IOException {
        MigrationLoader migrationLoader = new MigrationLoader(pathToMigrations);
        var migrations = migrationLoader.loadAll();
        Assertions.assertNotNull(migrations);
        Assertions.assertFalse(migrations.isEmpty());

        for (int i = 0; i < migrations.size(); i++) {
            var migration = migrations.get(i);
            Assertions.assertEquals(i+1, migration.version());
        }
    }

}
