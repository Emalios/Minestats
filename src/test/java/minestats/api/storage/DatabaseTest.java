package minestats.api.storage;

import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;
import fr.emalios.mystats.impl.storage.db.migrations.Migration;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationLoader;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static minestats.utils.Const.pathToMigrations;

public final class DatabaseTest {

    private static Connection connection;
    private static List<Migration> migrations = new MigrationLoader(pathToMigrations).loadAll();

    private DatabaseTest() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite::memory:");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public static void addMigration(Migration migration) {
        MigrationLoader migrationLoader = new MigrationLoader(pathToMigrations);
        migrations = migrationLoader.loadAll();
    }

    public static void makeMigrations() throws SQLException {
        MigrationRunner.migrate(getConnection(), migrations);
    }

    public static void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException ignored) {}
    }
}
