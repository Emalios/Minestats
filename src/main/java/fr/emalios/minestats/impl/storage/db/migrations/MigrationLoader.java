package fr.emalios.minestats.impl.storage.db.migrations;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static fr.emalios.minestats.MineStats.MODID;

public final class MigrationLoader {

    private static final Pattern FILE_PATTERN =
            Pattern.compile("^v(\\d+)__.+\\.sql$");

    private final String migrationsStringPath;

    public MigrationLoader(String migrationsStringPath) {
        this.migrationsStringPath = migrationsStringPath;
    }

    public Migration getNewest() {
        List<Migration> migrations = this.loadAll();

        if (migrations.isEmpty()) {
            throw new IllegalStateException(
                    "No migration found in: " + migrationsStringPath
            );
        }

        return migrations.getLast();
    }

    public List<Migration> loadAll() {
        IModFile modFile = ModList.get()
                .getModFileById(MODID)
                .getFile();

        Path migrationsPath = modFile.findResource(migrationsStringPath);

        if (!Files.exists(migrationsPath)) {
            throw new IllegalStateException(
                    "Migration directory not found: " + migrationsStringPath
            );
        }

        List<Migration> migrations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(migrationsPath)) {

            files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString()
                            .endsWith(".sql"))
                    .forEach(file -> loadMigration(file, migrations));

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to load migrations from: " + migrationsStringPath,
                    e
            );
        }

        migrations.sort(Comparator.comparingInt(Migration::version));

        checkDuplicateVersions(migrations);

        return migrations;
    }

    private static void loadMigration(
            Path file,
            List<Migration> migrations
    ) {
        String fileName = file.getFileName().toString();

        Matcher matcher = FILE_PATTERN.matcher(fileName);

        if (!matcher.matches()) {
            System.out.println(
                    "Ignoring migration file: " + fileName
            );
            return;
        }

        int version = Integer.parseInt(matcher.group(1));

        try {
            List<String> statements = parseSql(file);

            migrations.add(new Migration(
                    version,
                    fileName,
                    statements
            ));

        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to read migration: " + fileName,
                    e
            );
        }
    }

    private static List<String> parseSql(Path file) throws IOException {

        String content = Files.readString(
                file,
                StandardCharsets.UTF_8
        );

        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inBlockComment = false;

        for (String line : content.split("\\R")) {

            line = line.trim();

            // Empty line
            if (line.isEmpty()) {
                continue;
            }

            // Block comments /* ... */
            if (line.startsWith("/*")) {
                inBlockComment = true;
            }

            if (inBlockComment) {
                if (line.endsWith("*/")) {
                    inBlockComment = false;
                }

                continue;
            }

            // Line comments --
            if (line.startsWith("--")) {
                continue;
            }

            current
                    .append(line)
                    .append('\n');

            if (line.endsWith(";")) {
                statements.add(current.toString().trim());
                current.setLength(0);
            }
        }

        if (!current.isEmpty()) {
            statements.add(current.toString().trim());
        }

        return statements;
    }

    private static void checkDuplicateVersions(
            List<Migration> migrations
    ) {
        Set<Integer> seen = new HashSet<>();

        for (Migration migration : migrations) {

            if (!seen.add(migration.version())) {
                throw new IllegalStateException(
                        "Duplicate migration version: V"
                                + migration.version()
                );
            }
        }
    }
}