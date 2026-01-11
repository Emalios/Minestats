package minestats.api.storage;

import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;
import fr.emalios.mystats.impl.storage.db.migrations.MigrationRunner;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static minestats.utils.Const.pathToMigrations;

public final class DatabaseTest {

    private static Connection connection;

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

    public static void makeMigrations() throws SQLException {
        MigrationRunner.migrate(getConnection(), pathToMigrations);
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
