package fr.emalios.mystats.impl.storage.dao;

import fr.emalios.mystats.api.models.Position;

import java.sql.*;
import java.util.*;

/**
 * Represent a position of an inventory. Basically it is the composition of world, and pos x, y and z.
 * It will be linked to only one inventory
 */
public class InventoryPositionsDao {

    private final Connection connection;

    public InventoryPositionsDao(Connection connection) {
        this.connection = connection;
    }

    public int insert(int inventoryId, Position position) throws SQLException {
        String sql = """
            INSERT INTO inventory_pos (inventory_id, world, x, y, z)
            VALUES (?, ?, ?, ?, ?);
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setString(2, position.getWorld());
            ps.setInt(3, position.getX());
            ps.setInt(4, position.getY());
            ps.setInt(5, position.getZ());
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

    /**
     *
     * @return inventoryId associated with position
     * @throws SQLException
     */
    public Optional<Integer> findByPosition(Position position) throws SQLException {
        String sql = """
        SELECT inventory_id
        FROM inventory_pos
        WHERE world = ?
          AND x = ?
          AND y = ?
          AND z = ?
        LIMIT 1;
    """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, position.getWorld());
            ps.setInt(2, position.getX());
            ps.setInt(3, position.getY());
            ps.setInt(4, position.getZ());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getInt("inventory_id"));
                }
            }
        }
        return Optional.empty();
    }

    public Set<Integer> findAllByWorld(String world) throws SQLException {
        Set<Integer> inventories = new HashSet<>();
        String sql = """
            SELECT inventory_id FROM inventory_pos
            WHERE world = ?;
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, world);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                inventories.add(rs.getInt("inventory_id"));
            }
        }
        //connection.commit();
        return inventories;
    }


    public Set<Position> findAllByInvId(int inventoryId) throws SQLException {
        Set<Position> positions = new HashSet<>();
        String sql = """
            SELECT * FROM inventory_pos
            WHERE inventory_id = ?;
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                positions.add(new Position(
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                ));
            }
        }
        //connection.commit();
        return positions;
    }

    public boolean exist(Position position) throws SQLException {
        String sql = """
        SELECT 1
        FROM inventory_pos
          WHERE world = ?
          AND x = ?
          AND y = ?
          AND z = ?
        LIMIT 1;
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, position.getWorld());
            ps.setInt(2, position.getX());
            ps.setInt(3, position.getY());
            ps.setInt(4, position.getZ());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si une ligne existe
            }
        }
    }

    public boolean exist(int inventoryId, Position position) throws SQLException {
        String sql = """
        SELECT 1
        FROM inventory_pos
        WHERE inventory_id = ?
          AND world = ?
          AND x = ?
          AND y = ?
          AND z = ?
        LIMIT 1;
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setString(2, position.getWorld());
            ps.setInt(3, position.getX());
            ps.setInt(4, position.getY());
            ps.setInt(5, position.getZ());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si une ligne existe
            }
        }
    }


    public boolean delete(int inventoryId, Position position) throws SQLException {
        String sql = """
        DELETE FROM inventory_pos
        WHERE inventory_id = ?
          AND world = ?
          AND x = ?
          AND y = ?
          AND z = ?;
    """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setString(2, position.getWorld());
            ps.setInt(3, position.getX());
            ps.setInt(4, position.getY());
            ps.setInt(5, position.getZ());

            int rows = ps.executeUpdate();
            // connection.commit();
            return rows > 0;
        }
    }

}
