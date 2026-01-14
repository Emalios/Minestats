package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.PlayerInventoryRepository;
import fr.emalios.mystats.api.storage.PlayerRepository;
import fr.emalios.mystats.impl.storage.dao.PlayerDao;

import java.sql.SQLException;
import java.util.Optional;

public class SqlitePlayerRepository implements PlayerRepository {

    private final PlayerDao dao;
    private final PlayerInventoryRepository playerInventoryRepository;

    public SqlitePlayerRepository(PlayerDao dao, PlayerInventoryRepository playerInventoryRepository) {
        this.dao = dao;
        this.playerInventoryRepository = playerInventoryRepository;
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
        System.out.println("finding by name: " + name);
        var optPlayer = findByName(name);
        if (optPlayer.isPresent()) {
            StatPlayer statPlayer = optPlayer.get();
            this.playerInventoryRepository.hydrate(statPlayer);
            MyStats.LOGGER.debug("Player '{}' has been loaded with: ", name);
            MyStats.LOGGER.debug(statPlayer.getInventories().toString());
            return statPlayer;
        }

        StatPlayer statPlayer = new StatPlayer(name);
        this.save(statPlayer);
        return statPlayer;
    }


    @Override
    public Optional<StatPlayer> findByName(String name) {
        PlayerDao.PlayerRecord record;
        try {
            record = dao.findByName(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (record == null) return Optional.empty();
        StatPlayer statPlayer = new StatPlayer(record.name());
        statPlayer.assignId(record.id());
        this.playerInventoryRepository.hydrate(statPlayer);
        return Optional.of(statPlayer);
    }
}
