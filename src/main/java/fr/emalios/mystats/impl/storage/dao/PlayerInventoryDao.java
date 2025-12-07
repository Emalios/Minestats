package fr.emalios.mystats.impl.storage.dao;

import fr.emalios.mystats.impl.storage.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Link a player and his inventories
 */
public class PlayerInventoryDao {

    private final Connection connection;

    public PlayerInventoryDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(int playerId, int inventoryId) throws SQLException {
        String sql = """
            INSERT INTO player_inventories (player_id, inventory_id)
            VALUES (?, ?);
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, inventoryId);
            ps.executeUpdate();
            //connection.commit();
        }
    }

    public void insertIfNotExists(int playerId, int inventoryId) throws SQLException {
        if (!findByPlayerId(playerId).isEmpty()) return;
        this.insert(playerId, inventoryId);
    }

    public List<InventoryDao.InventoryRecord> findByPlayerName(String name) throws SQLException {
        PlayerDao.PlayerRecord record = Database.getInstance().getPlayerDao().findByName(name);
        if(record == null) return new ArrayList<>();
        String sql = "SELECT * FROM player_inventories WHERE player_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, record.id());
            ResultSet rs = ps.executeQuery();
            List<InventoryDao.InventoryRecord> records = new ArrayList<>();
            while (rs.next()) {
                records.add(new InventoryDao.InventoryRecord(
                        rs.getInt("id"),
                        rs.getString("block_id"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("type"),
                        rs.getString("created_at")
                ));
            }
            return records;
        }
    }

    public List<Integer> findInventoryIds(String playerName) throws SQLException {
        PlayerDao.PlayerRecord record = Database.getInstance().getPlayerDao().findByName(playerName);
        if(record == null) return new ArrayList<>();
        String sql = "SELECT inventory_id FROM player_inventories WHERE player_id = ?;";
        List<Integer> inventoryIds = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, record.id());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                inventoryIds.add(rs.getInt("inventory_id"));
            }
        }
        return inventoryIds;
    }

    public List<PlayerInventoryRecord> findByPlayerId(int playerId) throws SQLException {
        String sql = "SELECT * FROM player_inventories WHERE player_id = ?;";
        List<PlayerInventoryRecord> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PlayerInventoryRecord(
                        rs.getInt("player_id"),
                        rs.getInt("inventory_id"),
                        rs.getString("added_at")
                ));
            }
        }
        return list;
    }

    public record PlayerInventoryRecord(int playerId, int inventoryId, String addedAt) {}
}
