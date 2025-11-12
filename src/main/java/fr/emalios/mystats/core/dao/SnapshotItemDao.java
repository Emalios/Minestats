package fr.emalios.mystats.core.dao;

import fr.emalios.mystats.core.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent an item stored in a snapshot of an inventory. Associated with a snapshot.
 */
public class SnapshotItemDao {

    private final Connection connection;

    public SnapshotItemDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(int snapshotId, String itemName, int count) throws SQLException {
        String sql = """
            INSERT INTO snapshot_items (snapshot_id, item_name, count)
            VALUES (?, ?, ?);
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, snapshotId);
            ps.setString(2, itemName);
            ps.setInt(3, count);
            ps.executeUpdate();
            connection.commit();
        }
    }

    public List<ItemRecord> findBySnapshotId(int snapshotId) throws SQLException {
        String sql = "SELECT * FROM snapshot_items WHERE snapshot_id = ?;";
        List<ItemRecord> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, snapshotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new ItemRecord(
                        rs.getInt("id"),
                        rs.getInt("snapshot_id"),
                        rs.getString("item_name"),
                        rs.getInt("count")
                ));
            }
        }
        return items;
    }

    public record ItemRecord(int id, int snapshotId, String itemName, int count) {}
}
