package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SqliteInventoryRepository implements InventoryRepository {

    private final InventoryDao dao;

    public SqliteInventoryRepository(InventoryDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(Inventory inventory) {
        if(inventory.isPersisted()) throw new IllegalArgumentException("Inventory '" + inventory + "' is already persisted");
        try {
            int id = this.dao.insertIfNotExists(
                    inventory.getWorld(),
                    inventory.getX(),
                    inventory.getY(),
                    inventory.getZ(),
                    "UNUSED"
            );
            inventory.assignId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Inventory> findByPos(String world, int x, int y, int z) {
        Inventory inventory = new Inventory(world, x, y, z);
        try {
            Optional<InventoryDao.InventoryRecord> optInv = this.dao.getByPos(
                    inventory.getWorld(),
                    inventory.getX(), inventory.getY(), inventory.getZ());
            if (optInv.isPresent()) {
                inventory.assignId(optInv.get().id());
                return Optional.of(inventory);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Inventory getOrCreate(String world, int x, int y, int z) {
        Inventory inventory = new Inventory(world, x, y, z);
        try {
            Optional<InventoryDao.InventoryRecord> optInv = this.dao.getByPos(
                    inventory.getWorld(),
                    inventory.getX(), inventory.getY(), inventory.getZ());
            if (optInv.isPresent()) {
                inventory.assignId(optInv.get().id());
                return inventory;
            }
            this.save(inventory);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventory;
    }

    @Override
    public void delete(Inventory inventory) {
        if(!inventory.isPersisted()) throw new IllegalArgumentException("Inventory '" + inventory + "' is not persisted");
        try {
            this.dao.deleteById(inventory.getId());
            inventory.delete();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<Inventory> getAllFromWorld(String world) {
        List<Inventory> inventories = new ArrayList<>();
        try {
            var records = this.dao.getByWorld(world);
            for (InventoryDao.InventoryRecord record : records) {
                Inventory inventory = new Inventory(
                        record.world(), record.x(), record.y(), record.z()
                );
                inventory.assignId(record.id());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventories;
    }

    @Override
    public Collection<Inventory> getAll() {
        List<Inventory> inventories = new ArrayList<>();
        try {
            var records = this.dao.getAll();
            for (InventoryDao.InventoryRecord record : records) {
                Inventory inventory = new Inventory(
                        record.world(), record.x(), record.y(), record.z()
                );
                inventory.assignId(record.id());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventories;
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
