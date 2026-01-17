package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.models.Inventory;
import fr.emalios.mystats.api.models.Position;
import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;
import fr.emalios.mystats.impl.storage.dao.InventoryPositionsDao;

import java.sql.SQLException;
import java.util.*;

public class SqliteInventoryRepository implements InventoryRepository {

    private final InventoryDao inventoryDao;
    private final InventoryPositionsDao inventoryPositionsDao;

    public SqliteInventoryRepository(InventoryDao inventoryDao, InventoryPositionsDao inventoryPositionsDao) {
        this.inventoryDao = inventoryDao;
        this.inventoryPositionsDao = inventoryPositionsDao;
    }

    @Override
    public void save(Inventory inventory) {
        if(inventory.isPersisted()) throw new IllegalArgumentException("Inventory '" + inventory + "' is already persisted");
        try {
            //check that no positions already exists as one position can only be linked to one inventory TODO: support side?
            Set<Position> positions = inventory.getInvPositions();
            for (Position pos : positions) {
                if (inventoryPositionsDao.exist(pos)) {
                    throw new IllegalArgumentException("Inventory position '" + pos + "' already exists");
                }
            }
            int id = this.inventoryDao.insert(
                    "UNUSED"
            );
            //save positions
            for (Position position : positions) {
                this.inventoryPositionsDao.insert(id, position);
            }
            inventory.assignId(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Inventory> findByPos(Position position) {
        try {
            var optInvId = this.inventoryPositionsDao.findByPosition(position);
            if(optInvId.isEmpty()) return Optional.empty();
            var optInventory = this.inventoryDao.findById(optInvId.get());
            if(optInventory.isEmpty()) return Optional.empty();
            Inventory inventory = optInventory.get();
            //load positions
            var positions = this.inventoryPositionsDao.findAllByInvId(inventory.getId());
            for (Position pos : positions) {
                inventory.addPosition(pos);
            }
            return Optional.of(inventory);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Inventory getOrCreate(Position position) {
        Inventory inventory = new Inventory();
        try {
            var optInvId = this.inventoryPositionsDao.findByPosition(position);
            if(optInvId.isEmpty()) {
                inventory.addPosition(position);
                this.save(inventory);
                return inventory;
            }
            //get all associated positions
            int invId = optInvId.get();
            var positions = this.inventoryPositionsDao.findAllByInvId(invId);
            positions.forEach(inventory::addPosition);
            inventory.assignId(invId);
            return inventory;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Inventory inventory) {
        if(!inventory.isPersisted()) throw new IllegalArgumentException("Inventory '" + inventory + "' is not persisted");
        try {
            this.inventoryDao.deleteById(inventory.getId());
            inventory.delete();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Inventory> getAllFromWorld(String world) {
        Set<Inventory> inventories = new HashSet<>();
        try {
            var inventoriesIds = this.inventoryPositionsDao.findAllByWorld(world);
            for (Integer invId: inventoriesIds) {
                //get all pos for current inventory
                Inventory inventory = new Inventory();
                inventory.assignId(invId);
                var positions = this.inventoryPositionsDao.findAllByInvId(invId);
                inventory.addPositions(positions);
                inventories.add(inventory);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventories;
    }

    @Override
    public Set<Inventory> getAll() {
        Set<Inventory> inventories = new HashSet<>();
        try {
            var records = this.inventoryDao.getAll();
            for (Inventory inventory: records) {
                //get all pos for current inventory
                var positions = this.inventoryPositionsDao.findAllByInvId(inventory.getId());
                inventory.addPositions(positions);
                inventories.add(inventory);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventories;
    }

    @Override
    public Inventory getById(int id) {
        Optional<Inventory> invDb;
        Inventory inventory;
        try {
            invDb = this.inventoryDao.findById(id);
            if (invDb.isEmpty()) {
                throw new IllegalArgumentException("Inventory with id " + id + " does not exist");
            }
            var positions = this.inventoryPositionsDao.findAllByInvId(id);
            inventory = invDb.get();
            for (Position position : positions) {
                inventory.addPosition(position);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventory;
    }
}
