package fr.emalios.mystats.impl.storage.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SQLConsumer {
    void accept(Connection conn) throws SQLException;
}
