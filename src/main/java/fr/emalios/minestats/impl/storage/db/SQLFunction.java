package fr.emalios.minestats.impl.storage.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T> {
    T apply(Connection conn) throws SQLException;
}
