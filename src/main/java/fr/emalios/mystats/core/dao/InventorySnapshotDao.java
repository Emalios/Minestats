package fr.emalios.mystats.core.dao;

import fr.emalios.mystats.core.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a snapshot of an inventory. Basically it is the content of an inventory at a given time.
 * It will be linked to an inventory and associated with items (snapshot_items)
 */
public class InventorySnapshotDao {

    private final Connection connection;

    public InventorySnapshotDao(Connection connection) {
        this.connection = connection;
    }

    public int insert(int inventoryId, long timestamp) throws SQLException {
        String sql = "INSERT INTO inventory_snapshots (inventory_id, timestamp) VALUES (?, ?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setLong(2, timestamp);
            ps.executeUpdate();
            connection.commit();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public SnapshotRecord findById(int id) throws SQLException {
        String sql = "SELECT * FROM inventory_snapshots WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new SnapshotRecord(
                        rs.getInt("id"),
                        rs.getInt("inventory_id"),
                        rs.getLong("timestamp")
                );
            }
        }
        return null;
    }

    public List<SnapshotRecord> findAllByInvId(int inventoryId) throws SQLException {
        List<SnapshotRecord> list = new ArrayList<>();
        String sql = """
            SELECT * FROM inventory_snapshots
            WHERE inventory_id = ?
            ORDER BY timestamp DESC
            LIMIT 10;
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new SnapshotRecord(
                        rs.getInt("id"),
                        rs.getInt("inventory_id"),
                        rs.getLong("timestamp")
                ));
            }
        }
        return list;
    }

    public record SnapshotRecord(int id, int inventoryId, long timestamp) {}
}
