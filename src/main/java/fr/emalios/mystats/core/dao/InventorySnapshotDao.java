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

    public List<SnapshotRecord> findAll() throws SQLException {
        List<SnapshotRecord> list = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM inventory_snapshots;")) {
            while (rs.next()) {
                list.add(new SnapshotRecord(
                        rs.getInt("id"),
                        rs.getInt("inventory_id"),
                        rs.getInt("timestamp")
                ));
            }
        }
        return list;
    }

    public record SnapshotRecord(int id, int inventoryId, long timestamp) {}
}
