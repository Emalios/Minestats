package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.PlayerInventoryRepository;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;

import java.sql.SQLException;
import java.util.Collection;

public class SqlitePlayerInventoryRepository implements PlayerInventoryRepository {

    private final PlayerInventoryDao dao;

    public SqlitePlayerInventoryRepository(PlayerInventoryDao dao) {
        this.dao = dao;
    }

    @Override
    public void addInventory(StatPlayer statPlayer, Inventory inventory) {
        try {
            this.dao.insertIfNotExists(statPlayer.getId(), inventory.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeInventory(StatPlayer statPlayer, Inventory inventory) {
        try {
            return this.dao.delete(statPlayer.getId(), inventory.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasInventory(StatPlayer player, Inventory inventory) {
        try {
            return dao.exists(player.getId(), inventory.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Inventory> findByPlayer(StatPlayer statPlayer) {
        try {
            var records = dao.findInventoriesByPlayer(statPlayer.getId());
            return records.stream().map(r -> {
                Inventory inv = new Inventory(r.world(), r.x(), r.y(), r.z());
                inv.assignId(r.inventoryId());
                return inv;
            }).toList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
