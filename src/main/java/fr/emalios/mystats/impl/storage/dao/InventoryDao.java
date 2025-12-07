package fr.emalios.mystats.impl.storage.dao;

import fr.emalios.mystats.impl.storage.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represent an inventory.
 * blockId: unique identifier for a block composed of world:x;y;z
 * world: World where the inventory is scanned
 * x,y, z: position
 * type:
 * An inventory will be linked to a player (player_inventories) and to snapshots of this inventory (inventory_snapshots)
 */
public class InventoryDao {

    private final Connection connection;

    public InventoryDao(Connection connection) {
        this.connection = connection;
    }

    private String generateBlockId(String world, int x, int y, int z) {
        return world + ":" + x + ":" + y + ":" + z;
    }

    //TODO: review block_id system
    public int insert(String world, int x, int y, int z, String type) throws SQLException {
        String sql = """
            INSERT INTO inventories (block_id, world, x, y, z, type)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, generateBlockId(world, x, y, z));
            ps.setString(2, world);
            ps.setInt(3, x);
            ps.setInt(4, y);
            ps.setInt(5, z);
            ps.setString(6, type);
            ps.executeUpdate();
            //connection.commit();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM inventories WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            //connection.commit();
            return rows > 0;
        }
    }

    public int insertIfNotExists(String world, int x, int y, int z, String type) throws SQLException {
        if (!findByBlockId(world, x, y, z).isEmpty()) return -1; // déjà présent
        return insert(world, x, y, z, type);
    }

    public boolean exists(String world, int x, int y, int z) throws SQLException {
        return !findByBlockId(world, x, y, z).isEmpty();
    }

    public InventoryRecord findById(int id) throws SQLException {
        String sql = "SELECT * FROM inventories WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new InventoryRecord(
                        rs.getInt("id"),
                        rs.getString("block_id"),
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        rs.getString("type"),
                        rs.getString("created_at")
                );
            }
        }
        return null;
    }

    public Optional<InventoryRecord> findByBlockId(String world, int x, int y, int z) throws SQLException {
        String sql = "SELECT * FROM inventories WHERE block_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, generateBlockId(world, x, y, z));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new InventoryRecord(
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
        }
        return Optional.empty();
    }

    public List<InventoryRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM inventories;";
        List<InventoryRecord> inventories = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventories.add(new InventoryRecord(
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
        }
        return inventories;
    }

    public record InventoryRecord(
            int id, String blockId, String world, int x, int y, int z, String type, String createdAt
    ) {}
}
