package minestats.impl;

import fr.emalios.mystats.impl.storage.db.migrations.MigrationParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@DisplayName("Migrations parser test")
public class MigrationParserTest {

    private final String p1 = "mystats:migration/v1__init.sql";
    private final String p2 = "mystats:migration/v2__add_inv_pos.sql";

    @Test
    @DisplayName("Parse version")
    public void parseVersion() {
        Assertions.assertEquals(1, MigrationParser.parseVersion(p1));
        Assertions.assertEquals(2, MigrationParser.parseVersion(p2));
    }

    @Test
    @DisplayName("Parse name")
    public void parseName() {
        Assertions.assertEquals("v1__init.sql", MigrationParser.parseName(p1));
        Assertions.assertEquals("v2__add_inv_pos.sql", MigrationParser.parseName(p2));
    }

    @Test
    @DisplayName("Parse SQL")
    public void parseSQL() {
        //load content of file migration.sql with BufferedReader available at src/test/java/minestats/impl/migration.sql
        List<String> sql = null;
        try {
            sql = Files.readAllLines(
                    Path.of("src/test/java/minestats/impl/migration.sql")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var instructions = MigrationParser.parseSqlInstructions(sql);
        Assertions.assertNotNull(instructions);
        Assertions.assertEquals(3, instructions.size());
        Assertions.assertEquals("""
                PRAGMA foreign_keys = ON;
                """, instructions.get(0));
        Assertions.assertEquals("""
                CREATE TABLE IF NOT EXISTS players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
                """, instructions.get(1));
        Assertions.assertEquals("""
                CREATE TABLE IF NOT EXISTS inventory_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                inventory_id INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY (inventory_id) REFERENCES inventories(id) ON DELETE CASCADE
                );
                """, instructions.get(2));
    }

}
