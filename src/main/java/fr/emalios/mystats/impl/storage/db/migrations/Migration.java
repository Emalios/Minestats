package fr.emalios.mystats.impl.storage.db.migrations;


import java.util.List;

public record Migration(int version, String name, List<String> sqlStatements) {}

