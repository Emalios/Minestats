package fr.emalios.mystats.core.dao;

import fr.emalios.mystats.core.stat.CountUnit;
import fr.emalios.mystats.core.stat.Record;
import fr.emalios.mystats.core.stat.RecordType;

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

    public void insert(int snapshotId, String itemName, float count, RecordType recordType, CountUnit countUnit) throws SQLException {
        String sql = """
            INSERT INTO snapshot_items (snapshot_id, item_name, count, stat_type, countUnit)
            VALUES (?, ?, ?, ?, ?);
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, snapshotId);
            ps.setString(2, itemName);
            ps.setFloat(3, count);
            ps.setString(4, recordType.name());
            ps.setString(5, countUnit.name());
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.commit();
            System.out.println("CANT INSERT DATA TO DB: " + e.getMessage());
        }
    }

    public void insert(int snapshotId, Record record) throws SQLException {
        this.insert(snapshotId, record.getResourceId(), record.getCount(), record.getType(), record.getUnit());
    }

    public List<Record> findBySnapshotId(int snapshotId) throws SQLException {
        String sql = "SELECT * FROM snapshot_items WHERE snapshot_id = ?;";
        List<Record> items = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, snapshotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new Record(
                        RecordType.valueOf(rs.getString("stat_type")),
                        rs.getString("item_name"),
                        rs.getInt("count"),
                        CountUnit.valueOf(rs.getString("countUnit"))
                ));
            }
        }
        return items;
    }

    public record ItemRecord(int id, int snapshotId, String itemName, int count, RecordType recordType, CountUnit countUnit) {}
}
