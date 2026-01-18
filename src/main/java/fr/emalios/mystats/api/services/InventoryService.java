package fr.emalios.mystats.api.services;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.models.*;
import fr.emalios.mystats.api.models.inventory.*;
import fr.emalios.mystats.api.storage.*;

import java.util.*;

public class InventoryService {

    private final PlayerInventoryRepository playerInventoryRepository;
    private final InventoryRepository inventoryRepository;
    private final InventorySnapshotRepository inventorySnapshotRepository;
    private final IHandlerLoader iHandlerLoader;
    private final InventoryPositionsRepository inventoryPositionsRepository;

    private Map<Integer, Inventory> loadedInventories = new HashMap<>();

    public InventoryService(InventoryRepository inventoryRepository, PlayerInventoryRepository playerInventoryRepository,
                            InventorySnapshotRepository inventorySnapshotRepository, IHandlerLoader iHandlerLoader, InventoryPositionsRepository inventoryPositionsRepository) {
        this.inventoryRepository = inventoryRepository;
        this.playerInventoryRepository = playerInventoryRepository;
        this.inventorySnapshotRepository = inventorySnapshotRepository;
        this.iHandlerLoader = iHandlerLoader;
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
                this.addHandlersToInventory(inventory);
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
        List<Integer> toRemove = new ArrayList<>();
        for (Integer id : this.loadedInventories.keySet()) {
            var inventory = this.loadedInventories.get(id);
            if(!inventory.isPersisted()) {
                MyStats.LOGGER.debug("A non persisted inventory was found [{}]", id);
                toRemove.add(id);
                continue;
            }
            if(!inventory.hasHandlers()) {
                toRemove.add(id);
                this.inventoryRepository.delete(inventory);
            } else this.recordInventoryContent(inventory);
        }
        toRemove.forEach(id -> this.loadedInventories.remove(id));
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
            this.addHandlersToInventory(inv);
            return inv;
        }
    }

    public List<Snapshot> getSnapshots(Inventory inventory) {
        return this.inventorySnapshotRepository.findAllByInventory(inventory);
    }

    public List<Snapshot> getLastSnapshots(Inventory inventory, int limit) {
        return this.inventorySnapshotRepository.findLastByInventory(inventory, limit);
    }

    public void loadAll() {
        var inventories = this.inventoryRepository.getAll();
        for (Inventory inv : inventories) {
            int id = inv.getId();
            if(!this.loadedInventories.containsKey(id)) {
                this.loadedInventories.put(id, inv);
                this.addHandlersToInventory(inv);
            }
        }
    }

    public Collection<Inventory> getAll() {
        this.loadAll();
        return this.loadedInventories.values();
    }

    public void removePositionFromInventory(Inventory inventory, Position position) {
        //get the loaded inventory
        var localInv = this.loadedInventories.get(inventory.getId());
        this.inventoryPositionsRepository.removePosition(localInv, position);
    }

    /**
     * Delete all present inventories in storage and void the cache
     */
    public void deleteAll() {
        this.inventoryRepository.deleteAll();
        this.getAll().forEach(Inventory::delete);
        this.loadedInventories.clear();
    }

    /**
     * Delete a persisted inventory and unload it. should not be used when iterating over the loaded inventories map
     * @param inventory to remove
     */
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
            if(inventory.containsPosition(position)) {
                this.addHandlersToInventory(inventory);
                return Optional.of(inventory);
            }
        }
        return Optional.empty();
    }

    public void create(Inventory inventory) {
        this.inventoryRepository.save(inventory);
        this.loadedInventories.put(inventory.getId(), inventory);
        this.addHandlersToInventory(inventory);
    }

    public boolean isLoaded(Inventory inventory) {
        return this.loadedInventories.containsValue(inventory);
    }

    private void addHandlersToInventory(Inventory inventory) {
        var allPositions = inventory.getInvPositions();
        if(allPositions.isEmpty()) return;

        //ensure every position have the same handlers
        Collection<IHandler> first = this.iHandlerLoader.loadHandlers(allPositions.iterator().next());
        for (Position invPosition : inventory.getInvPositions()) {
            Collection<IHandler> handlers = this.iHandlerLoader.loadHandlers(invPosition);
            if(handlers.isEmpty() || !handlers.equals(first)) {
                this.removePositionFromInventory(inventory, invPosition);
            }
        }
        //test if every handler got are non-existing
        inventory.addHandlers(first);
        if(!inventory.hasHandlers()) {
            this.deleteInventory(inventory);
        }
    }
}
