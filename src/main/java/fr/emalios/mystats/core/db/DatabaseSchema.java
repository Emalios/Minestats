package fr.emalios.mystats.core.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSchema {

    public static void createAll() throws SQLException {
        Connection conn = Database.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {

            // Table players
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Table inventories
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    block_id TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x INTEGER NOT NULL,
                    y INTEGER NOT NULL,
                    z INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // Table inventory_snapshots
            // put integer for timestamp but don't know if it's the best solution
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory_snapshots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    inventory_id INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL,
                    FOREIGN KEY (inventory_id) REFERENCES inventories(id)
                );
            """);

            // Table snapshot_items
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS snapshot_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    snapshot_id INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    count REAL NOT NULL,
                    stat_type TEXT NOT NULL,
                    countUnit TEXT NOT NULL,
                    FOREIGN KEY (snapshot_id) REFERENCES inventory_snapshots(id)
                );
            """);

            // Table player_inventories (relation plusieurs-à-plusieurs)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_inventories (
                    player_id INTEGER NOT NULL,
                    inventory_id INTEGER NOT NULL,
                    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (player_id, inventory_id),
                    FOREIGN KEY (player_id) REFERENCES players(id),
                    FOREIGN KEY (inventory_id) REFERENCES inventories(id)
                );
            """);

            conn.commit();
        }
    }

    public static void showDbStructures() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        try (Statement stmt = connection.createStatement();
             ResultSet tables = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")) {

            while (tables.next()) {
                String tableName = tables.getString("name");
                System.out.println("🔹 Table: " + tableName);

                // Colonnes via PRAGMA
                try (Statement colStmt = connection.createStatement();
                     ResultSet cols = colStmt.executeQuery("PRAGMA table_info('" + tableName + "');")) {

                    while (cols.next()) {
                        String colName = cols.getString("name");
                        String colType = cols.getString("type");
                        boolean notNull = cols.getInt("notnull") == 1;
                        String dflt = cols.getString("dflt_value");
                        boolean pk = cols.getInt("pk") == 1;

                        System.out.printf("   - %-20s %-10s %s Default: %s %s%n",
                                colName,
                                colType,
                                notNull ? "NOT NULL" : "NULL",
                                dflt,
                                pk ? "(PK)" : ""
                        );
                    }
                }

                System.out.println();
            }

            System.out.println("===============================\n");
            connection.commit();

        }
    }

    public static void deleteDb() {
        Database.getInstance().executeWriteAsync(conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys=OFF;");
            }

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';"
                 )) {

                while (rs.next()) {
                    String table = rs.getString("name");
                    System.out.println("Dropping table: " + table);

                    try (Statement drop = conn.createStatement()) {
                        drop.execute("DROP TABLE IF EXISTS \"" + table + "\";");
                    }
                }
            }

            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys=ON;");
            }

            conn.commit();
        });
    }

    public static void olddeleteDb() throws SQLException {

        Connection connection = Database.getInstance().getConnection();

        System.out.println("deleteDb() called from thread: " + Thread.currentThread().getName());

        try (Statement s = connection.createStatement()) {
            s.execute("PRAGMA foreign_keys = OFF;");
        }

        List<String> tables = new ArrayList<>();

        try (Statement readStmt = connection.createStatement();
             ResultSet rs = readStmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")) {

            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        }

        try (Statement dropStmt = connection.createStatement()) {

            for (String t : tables) {
                System.out.println("About to drop '" + t + "' by thread = " + Thread.currentThread().getName());

                try {
                    dropStmt.execute("DROP TABLE IF EXISTS \"" + t + "\";");
                } catch (SQLException e) {
                    if (e.getMessage().contains("SQLITE_BUSY")) {
                        System.err.println("===== SQLITE BUSY when dropping '" + t + "' =====");
                        e.printStackTrace();
                    }
                    throw e;
                }
            }
        }

        try (Statement s = connection.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
        }

        connection.commit();
    }


}
