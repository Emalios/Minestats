package fr.emalios.mystats.impl.storage.db.migrations;

import org.slf4j.Logger;

import java.sql.*;
import java.util.List;

public final class MigrationRunner {

    public static void migrate(Connection conn, List<Migration> migrations, Logger logger) throws SQLException {
        logger.info("Starting migrations... Available: {}", migrations);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        }

        int currentVersion = getCurrentVersion(conn);
        logger.info("Current version: {}", currentVersion);
        System.out.println("Current version: " + currentVersion);

        for (Migration m : migrations) {
            if (m.version() > currentVersion) {
                logger.info("Applying migration {}", m.version());
                applyMigration(conn, m);
                logger.info("Migration applied");
            }
        }
        logger.info("Migrations finished.");
    }

    public static int getCurrentVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER NOT NULL
                )
            """);

            try (ResultSet rs = st.executeQuery(
                    "SELECT version FROM schema_version LIMIT 1"
            )) {
                if (rs.next()) return rs.getInt(1);
            }

            st.execute("INSERT INTO schema_version(version) VALUES (0)");
            return 0;
        }
    }

    private static void setCurrentVersion(Connection conn, int version)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE schema_version SET version = ?"
        )) {
            ps.setInt(1, version);
            int r = ps.executeUpdate();
        }
    }

    public static void applyMigration(Connection conn, Migration m)
            throws SQLException {

        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            for (String sql : m.sqlStatements()) {
                st.execute(sql);
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            setCurrentVersion(conn, m.version());
        }
    }
}
