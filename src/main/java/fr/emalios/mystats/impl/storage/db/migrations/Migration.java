package fr.emalios.mystats.impl.storage.db.migrations;


import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Migration(int version, String name, List<String> sqlStatements) implements Comparable<Migration> {

    @Override
    public String toString() {
        return "Migration{" +
                "version=" + version +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NotNull Migration o) {
        return Integer.compare(this.version(), o.version());
    }
}

