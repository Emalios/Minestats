package fr.emalios.mystats.core.dao;

import fr.emalios.mystats.core.db.Database;

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
            connection.commit();
        }
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
