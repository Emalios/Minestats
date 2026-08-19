package fr.emalios.minestats.impl.storage.repository;

import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.api.storage.InventoryPositionsRepository;
import fr.emalios.minestats.impl.storage.dao.InventoryPositionsDao;

import java.sql.SQLException;
import java.util.*;

public class SqliteInventoryPositionsRepository implements InventoryPositionsRepository {

    private final InventoryPositionsDao dao;

    public SqliteInventoryPositionsRepository(InventoryPositionsDao dao) {
        this.dao = dao;
    }

    @Override
    public void addPosition(Inventory inventory, Position position) {
        try {
            this.dao.insert(inventory.getId(), position);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removePosition(Inventory inventory, Position position) {
        try {
            return this.dao.delete(inventory.getId(), position);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPosition(Inventory inventory, Position position) {
        try {
            return this.dao.exist(inventory.getId(), position);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Position> findAllByInventory(Inventory inventory) {
        return this.findAllByInventoryId(inventory.getId());
    }

    @Override
    public Set<Position> findAllByInventoryId(int inventoryId) {
        try {
            return this.dao.findAllByInvId(inventoryId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
