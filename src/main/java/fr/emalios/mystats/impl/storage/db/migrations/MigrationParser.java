package fr.emalios.mystats.impl.storage.db.migrations;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MigrationParser {

    public static Migration parse(String path, List<String> fileContent) {
        int version = parseVersion(path);
        return new Migration(version, parseName(path), parseSqlInstructions(fileContent));
    }

    public static int parseVersion(String path) {
        String fileName = path.substring(path.lastIndexOf('/') + 1);

        int end = fileName.indexOf("__");
        return Integer.parseInt(fileName.substring(1, end));
    }

    public static String parseName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static List<String> parseSqlInstructions(List<String> lines) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inBlockComment = false;

        for (String rawLine : lines) {

            String line = rawLine.trim();

            if (line.isEmpty()) continue;

            // /* block comments */
            if (line.startsWith("/*")) {
                inBlockComment = true;
            }
            if (inBlockComment) {
                if (line.endsWith("*/")) {
                    inBlockComment = false;
                }
                continue;
            }

            // -- line comments
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




}
