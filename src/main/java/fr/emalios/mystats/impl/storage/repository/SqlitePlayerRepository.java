package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.PlayerRepository;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;

import java.sql.SQLException;

public class SqlitePlayerRepository implements PlayerRepository {

    private final PlayerDao dao;

    public SqlitePlayerRepository(PlayerDao dao) {
        this.dao = dao;
    }

    @Override
    public boolean hasInventory(Inventory inventory) {
        return false;
    }

    @Override
    public void save(StatPlayer statPlayer) {
        String name = statPlayer.getName();
        if(statPlayer.isPersisted()) throw new IllegalArgumentException("Player '" + name + "' already exists");
        try {
            int id = this.dao.insertIfNotExists(name);
            statPlayer.assignId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StatPlayer getOrCreate(String name) {
        StatPlayer existing = findByName(name);
        if (existing != null) {
            return existing;
        }

        StatPlayer statPlayer = new StatPlayer(name);
        this.save(statPlayer);
        return statPlayer;
    }

    @Override
    public StatPlayer findByName(String name) {
        PlayerDao.PlayerRecord record;
        try {
            record = dao.findByName(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (record == null) throw new IllegalArgumentException("Player '" + name + "' not found");
        StatPlayer statPlayer = new StatPlayer(record.name());
        statPlayer.assignId(record.id());
        return statPlayer;
    }
}
