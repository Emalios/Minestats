package fr.emalios.mystats.core.dao;

import fr.emalios.mystats.core.db.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represent a player, a player is simply a name and will be associated with inventories with player_inventories
 */
public class PlayerDao {

    private final Connection connection;

    public PlayerDao(Connection connection) {
        this.connection = connection;
    }

    public int insert(String name) throws SQLException {
        String sql = "INSERT INTO players (name) VALUES (?);";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
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

    public int insertIfNotExists(String name) throws SQLException {
        if (findByName(name) != null) return -1; // déjà présent
        return insert(name);
    }

    public PlayerRecord findById(int id) throws SQLException {
        String sql = "SELECT * FROM players WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PlayerRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("created_at")
                );
            }
        }
        return null;
    }

    public PlayerRecord findByName(String name) throws SQLException {
        String sql = "SELECT * FROM players WHERE name = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new PlayerRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("created_at")
                );
            }
        }
        return null;
    }

    public List<PlayerRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM players;";
        List<PlayerRecord> players = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                players.add(new PlayerRecord(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("created_at")
                ));
            }
        }
        return players;
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM players WHERE id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            connection.commit();
        }
    }



    /** Simple record pour représenter un joueur. */
    public record PlayerRecord(int id, String name, String createdAt) {}
}
