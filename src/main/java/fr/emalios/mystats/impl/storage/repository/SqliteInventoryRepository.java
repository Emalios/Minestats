package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;

import java.sql.SQLException;

public class SqliteInventoryRepository implements InventoryRepository {

    private final InventoryDao dao;

    public SqliteInventoryRepository(InventoryDao dao) {
        this.dao = dao;
    }

    @Override
    public Inventory getById(int id) {
        InventoryDao.InventoryRecord invDb;
        try {
            invDb = this.dao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Inventory inventory = new Inventory(invDb.world(), invDb.x(), invDb.y(), invDb.z());
        inventory.assignId(invDb.id());
        return inventory;
    }
}
