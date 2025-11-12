package fr.emalios.mystats.core.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for handling the database, creating/modify/deleting tables etc.
 */
public class DatabaseManager {


    public static void createTables(Connection connection) {
        try (Statement stmt = connection.createStatement()) {

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
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory_snapshots (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    inventory_id INTEGER NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (inventory_id) REFERENCES inventories(id)
                );
            """);

            // Table snapshot_items
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS snapshot_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    snapshot_id INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    count INTEGER NOT NULL,
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

            System.out.println("Tables créées ou déjà existantes.");
            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void showDbStructures(Connection connection) {
        if (connection == null) {
            System.out.println("Null connection.");
            return;
        }

        try {
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
            }

            System.out.println("===============================\n");
            connection.commit();

        } catch (SQLException e) {
            System.err.println("Erreur lors de l’inspection de la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void deleteDb(Connection connection) {
        if (connection == null) {
            System.out.println("Null connection.");
            return;
        }

        try {

            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = OFF;");
            }

            List<String> tables = new ArrayList<>();
            try (Statement readStmt = connection.createStatement();
                 ResultSet rs = readStmt.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")) {
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            }

            // 2️⃣ Supprimer chaque table après avoir fermé le ResultSet
            try (Statement dropStmt = connection.createStatement()) {
                for (String tableName : tables) {
                    System.out.println("Delete '" + tableName + "' table.");
                    dropStmt.execute("DROP TABLE IF EXISTS \"" + tableName + "\";");
                }
            }

            try (Statement pragmaStmt = connection.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys = ON;");
            }

            connection.commit();

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Erreur lors de la suppression des tables : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
