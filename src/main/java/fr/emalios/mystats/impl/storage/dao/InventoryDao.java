package fr.emalios.mystats.impl.storage.dao;

import fr.emalios.mystats.api.models.inventory.Inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Represent an inventory.
 * blockId: unique identifier for a block composed of world:x;y;z
 * world: World where the inventory is scanned
 * x,y,z: position
 * type:
 * An inventory will be linked to a player (player_inventories) and to snapshots of this inventory (inventory_snapshots)
 */
public class InventoryDao {

    private Connection connection;

    public InventoryDao(Connection connection) {
        this.connection = connection;
    }

    public int insert(String type) throws SQLException {
        String sql = """
            INSERT INTO inventories (type)
            VALUES (?);
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, type);
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

    public boolean deleteAll() throws SQLException {
        String sql = "DELETE FROM inventories;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            //connection.commit();
            return rows > 0;
        }
    }

    public Optional<Inventory> findById(int id) throws SQLException {
        String sql = "SELECT * FROM inventories WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int invId = rs.getInt("id");
                Inventory inventory = new Inventory();
                inventory.assignId(invId);
                return Optional.of(inventory);
            }
        }
        return Optional.empty();
    }

    public Optional<InventoryRecord> getByPos(String world, int x, int y, int z) throws SQLException {
        String sql = "SELECT * FROM inventories WHERE world = ? AND x = ? AND y = ? AND z = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new InventoryRecord(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("created_at")
                ));
            }
        }
        return Optional.empty();
    }

    public List<InventoryRecord> getByWorld(String world) throws SQLException {
        String sql = "SELECT * FROM inventories WHERE world = ?;";
        List<InventoryRecord> records = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, world);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(new InventoryRecord(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("created_at")
                ));
            }
        }
        return records;
    }

    public Collection<Inventory> getAll() throws SQLException {
        String sql = "SELECT * FROM inventories;";
        List<Inventory> inventories = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Inventory inventory = new Inventory();
                inventory.assignId(rs.getInt("id"));
                inventories.add(inventory);
            }
        }
        return inventories;
    }

    public record InventoryRecord(
            int id, String type, String createdAt
    ) {}
}
