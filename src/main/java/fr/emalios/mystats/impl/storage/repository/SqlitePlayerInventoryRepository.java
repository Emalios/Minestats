package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.StatPlayer;
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
            statPlayer.addInventory(inventory);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeInventory(StatPlayer statPlayer, Inventory inventory) {
        try {
            boolean deleted = this.dao.delete(statPlayer.getId(), inventory.getId());
            if(deleted) statPlayer.removeInventory(inventory);
            return deleted;
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
        return dao.findInventoriesByPlayer(statPlayer.getId());
    }

    @Override
    public void hydrate(StatPlayer statPlayer) {
        Collection<Inventory> records = this.findByPlayer(statPlayer);
        statPlayer.loadInventories(records);
    }
}
