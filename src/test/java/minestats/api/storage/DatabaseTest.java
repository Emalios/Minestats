package minestats.api.storage;

import fr.emalios.mystats.impl.storage.db.DatabaseInitializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseTest {

    private static Connection connection;

    private DatabaseTest() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                connection.setAutoCommit(true);

                DatabaseInitializer.createAll(connection);
                DatabaseInitializer.createIndexes(connection);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
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
