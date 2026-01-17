package fr.emalios.mystats.impl.storage.dao;

import fr.emalios.mystats.api.models.CountUnit;
import fr.emalios.mystats.api.models.Record;
import fr.emalios.mystats.api.models.RecordType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represent a thing stored in a snapshot of an inventory. Associated with a snapshot.
 */
public class RecordDao {

    private final Connection connection;

    public RecordDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(int snapshotId, String itemName, float count, RecordType recordType, CountUnit countUnit) {
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
            //connection.commit();
        } catch (SQLException e) {
            //connection.commit();
            System.out.println("CANT INSERT DATA TO DB: " + e.getMessage());
        }
    }

    public void insert(int snapshotId, Collection<Record> records) throws SQLException {
        String sql = """
            INSERT INTO snapshot_items (snapshot_id, item_name, count, stat_type, countUnit)
            VALUES (?, ?, ?, ?, ?);
        """;
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        for (Record record : records) {
            this.addRecordToStatement(ps, record, snapshotId);
            ps.addBatch();
        }

        ps.executeBatch();
        connection.commit();
    }

    public void insert(int snapshotId, Record record) {
        this.insert(snapshotId, record.getResourceId(), record.getCount(), record.getType(), record.getUnit());
    }

    private void addRecordToStatement(PreparedStatement ps, Record record, int snapshotId) throws SQLException {
        ps.setInt(1, snapshotId);
        ps.setString(2, record.getResourceId());
        ps.setFloat(3, record.getCount());
        ps.setString(4, record.getType().name());
        ps.setString(5, record.getUnit().name());
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
