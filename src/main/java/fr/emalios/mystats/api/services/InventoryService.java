package fr.emalios.mystats.api.services;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.models.*;
import fr.emalios.mystats.api.storage.*;

import java.util.*;

public class InventoryService {

    private final PlayerInventoryRepository playerInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final InventoryPositionsRepository inventoryPositionsRepository;

    private Map<Integer, Inventory> loadedInventories = new HashMap<>();

    public InventoryService(InventoryRepository inventoryRepository, PlayerInventoryRepository playerInventoryRepository,
                            InventorySnapshotRepository inventorySnapshotRepository, InventoryPositionsRepository inventoryPositionsRepository) {
        this.inventoryRepository = inventoryRepository;
        this.playerInventoryRepository = playerInventoryRepository;
        this.inventorySnapshotRepository = inventorySnapshotRepository;
        this.inventoryPositionsRepository = inventoryPositionsRepository;
    }

    public void loadInventories(StatPlayer statPlayer) {
        //either get the already loaded inventory or insert it to the map
        for (Inventory inventory : this.playerInventoryRepository.findByPlayer(statPlayer)) {
            int id = inventory.getId();
            if (loadedInventories.containsKey(id)) {
                statPlayer.addInventory(loadedInventories.get(id));
            } else {
                loadedInventories.put(id, inventory);
                statPlayer.addInventory(inventory);
            }
        }
    }

    /**
     * TODO: maybe a system to detect if the inventory has changed since last scan is possible
     * Method responsible to scan the content of every monitored blocks
     * for each block we:
     * - create an inventory snapshot
     * - scan inventory content
     * - for each item create a snapshot item
     */
    public void scan() {
        for (Integer id : this.loadedInventories.keySet()) {
            var inventory = this.loadedInventories.get(id);
            if(!inventory.isPersisted()) {
                MyStats.LOGGER.debug("A non persisted inventory was found [{}]", id);
                this.loadedInventories.remove(id);
                continue;
            }
            if(!inventory.isValid()) this.deleteInventory(inventory);
            else this.recordInventoryContent(inventory);
        }
    }

    /**
     * Create an inventory snapshot and save it to storage implementation
     * <!> Inventory must exist in storage implementation </!>
     */
    public void recordInventoryContent(Inventory inventory) {
        this.inventorySnapshotRepository.addSnapshot(inventory.createSnapshot());
    }

    public Inventory getOrCreate(Position position) {
        //either scan all loadedInvs to check if position is in one inventory
        //or ask db to get optional associated id. This for the moment
        var inv = this.inventoryRepository.getOrCreate(position);
        //check if need to be loaded
        if(this.loadedInventories.containsKey(inv.getId())) {
            return this.loadedInventories.get(inv.getId());
        } else {
            this.loadedInventories.put(inv.getId(), inv);
            return inv;
        }
    }

    public List<Snapshot> getSnapshots(Inventory inventory) {
        return this.inventorySnapshotRepository.findAllByInventory(inventory);
    }

    public List<Snapshot> getLastSnapshots(Inventory inventory, int limit) {
        return this.inventorySnapshotRepository.findLastByInventory(inventory, limit);
    }

    public Collection<Inventory> getAll() {
        var inventories = this.inventoryRepository.getAll();
        for (Inventory inv : inventories) {
            int id = inv.getId();
            if(!this.loadedInventories.containsKey(id)) {
                this.loadedInventories.put(id, inv);
            }
        }
        System.out.println("all inventories:");
        this.loadedInventories.forEach((id, inv) -> {
            System.out.println(inv);
        });
        System.out.println("===========");
        return this.loadedInventories.values();
    }

    public void removePositionFromInventory(Inventory inventory, Position position) {
        //get the loaded inventory
        var localInv = this.loadedInventories.get(inventory.getId());
        this.inventoryPositionsRepository.removePosition(localInv, position);
    }

    public void addInventoryToPlayer(StatPlayer statPlayer, Inventory inventory) {
        this.playerInventoryRepository.addInventory(statPlayer, inventory);
        statPlayer.addInventory(inventory);
    }

    public void removeInventoryFromPlayer(StatPlayer statPlayer, Inventory inventory) {
        this.playerInventoryRepository.removeInventory(statPlayer, inventory);
    }

    public void deleteInventory(Inventory inventory) {
        this.loadedInventories.remove(inventory.getId());
        this.inventoryRepository.delete(inventory);
    }

    /**
     * Only look in loaded inventories
     * @param position to get inventory from
     * @return inventory who has the associated position
     */
    public Optional<Inventory> findByPos(Position position) {
        for (Inventory inventory : this.loadedInventories.values()) {
            if(inventory.containsPosition(position)) {return Optional.of(inventory);}
        }
        return Optional.empty();
    }

    public void create(Inventory inventory) {
        this.inventoryRepository.save(inventory);
        this.loadedInventories.put(inventory.getId(), inventory);
    }

}
