package fr.emalios.mystats.impl.storage.dao;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Position;
import fr.emalios.mystats.impl.storage.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (exists(playerId, inventoryId)) return;
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

    private void debugPlayerInventories(int playerId) throws SQLException {
        String debugSql = """
        SELECT player_id, inventory_id
        FROM player_inventories
        WHERE player_id = ?;
    """;

        try (PreparedStatement ps = connection.prepareStatement(debugSql)) {
            ps.setInt(1, playerId);
            ResultSet rs = ps.executeQuery();

            System.out.println("DEBUG inventories for player_id=" + playerId);
            while (rs.next()) {
                System.out.println(
                        "player_id=" + rs.getInt("player_id") +
                                ", inventory_id=" + rs.getInt("inventory_id")
                );
            }
        }
    }


    public boolean exists(int playerId, int inventoryId) throws SQLException {
        String sql = """
        SELECT 1 FROM player_inventories
        WHERE player_id = ? AND inventory_id = ?
        LIMIT 1;
    """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, inventoryId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
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

    public List<Inventory> findInventoriesByPlayer(int playerId) {

        String sql = """
        SELECT i.id AS inventory_id,
               p.world,
               p.x,
               p.y,
               p.z
        FROM inventories i
        JOIN player_inventories pi ON pi.inventory_id = i.id
        JOIN inventory_pos p ON p.inventory_id = i.id
        WHERE pi.player_id = ?;
    """;

        Map<Integer, Inventory> inventoryMap = new HashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int invId = rs.getInt("inventory_id");
                    String world = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");

                    Inventory inventory = inventoryMap.computeIfAbsent(invId, id -> {
                        Inventory inv = new Inventory();
                        inv.assignId(id);
                        return inv;
                    });

                    inventory.addPosition(new Position(world, x, y, z));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(inventoryMap.values());
    }


    public boolean delete(int playerId, int inventoryId) throws SQLException {
        String sql = """
        DELETE FROM player_inventories
        WHERE player_id = ? AND inventory_id = ?;
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setInt(2, inventoryId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
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
