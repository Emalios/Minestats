package fr.emalios.mystats.api;

import fr.emalios.mystats.api.storage.InventoryRepository;
import fr.emalios.mystats.api.storage.Storage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryManager {

    private final InventoryRepository inventoryRepository;
    private Set<Inventory> loadedInventories = new HashSet<>();

    public InventoryManager(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Set<Inventory> loadAll() {
        this.loadedInventories = this.inventoryRepository.getAll();
        return Set.copyOf(this.loadedInventories);
    }

}
