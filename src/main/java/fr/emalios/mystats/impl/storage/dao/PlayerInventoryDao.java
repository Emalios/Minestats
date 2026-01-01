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

    public List<PlayerInventoryWithInventoryRecord> findInventoriesByPlayer(int playerId)
            throws SQLException {

        String sql = """
            SELECT i.id, i.world, i.x, i.y, i.z
            FROM inventories i
            JOIN player_inventories pi ON pi.inventory_id = i.id
            WHERE pi.player_id = ?;
        """;

        List<PlayerInventoryWithInventoryRecord> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(new PlayerInventoryWithInventoryRecord(
                        rs.getInt("id"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                ));
            }
        }

        return result;
    }

    public void delete(int playerId, int inventoryId) throws SQLException {
        String sql = """
        DELETE FROM player_inventories
        WHERE player_id = ? AND inventory_id = ?;
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, inventoryId);
            ps.executeUpdate();
            // connection.commit();
        }
    }

    public record PlayerInventoryWithInventoryRecord(
            int inventoryId,
            String world,
            int x,
            int y,
            int z
    ) {}
    public record PlayerInventoryRecord(int playerId, int inventoryId, String addedAt) {}
}
