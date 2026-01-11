package fr.emalios.mystats.impl.storage.db.migrations;

import fr.emalios.mystats.impl.storage.db.migrations.Migration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MigrationLoader {

    private static final Pattern FILE_PATTERN =
            Pattern.compile("^V(\\d+)__.+\\.sql$");

    private final Path path;

    public MigrationLoader(Path path) {
        this.path = path;
    }

    public Migration getNewest() {
        return this.loadAll().getLast();
    }

    public List<Migration> loadAll() {

        if (!Files.exists(this.path)) {
            throw new IllegalStateException(
                    "Migration directory not found: " + this.path
            );
        }

        List<Migration> migrations = new ArrayList<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(this.path, "*.sql")) {

            for (Path file : stream) {

                String fileName = file.getFileName().toString();
                Matcher matcher = FILE_PATTERN.matcher(fileName);

                if (!matcher.matches()) {
                    System.out.println("Ignoring migration file: " + fileName);
                    continue;
                }

                int version = Integer.parseInt(matcher.group(1));

                List<String> statements = parseSql(file);

                migrations.add(new Migration(
                        version,
                        fileName,
                        statements
                ));
            }
        } catch (IOException e) {
            return List.of();
        }

        migrations.sort(Comparator.comparingInt(Migration::version));
        checkDuplicateVersions(migrations);

        return migrations;
    }

    private static List<String> parseSql(Path file) throws IOException {

        String content = Files.readString(file, StandardCharsets.UTF_8);

        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inBlockComment = false;

        for (String line : content.split("\n")) {

            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

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
            if (line.startsWith("--")) continue;

            current.append(line).append('\n');

            if (line.endsWith(";")) {
                statements.add(current.toString());
                current.setLength(0);
            }
        }

        if (!current.isEmpty()) {
            statements.add(current.toString());
        }

        return statements;
    }

    private void checkDuplicateVersions(List<Migration> migrations) {

        Set<Integer> seen = new HashSet<>();

        for (Migration m : migrations) {
            if (!seen.add(m.version())) {
                throw new IllegalStateException(
                        "Duplicate migration version: V" + m.version()
                );
            }
        }
    }
}
