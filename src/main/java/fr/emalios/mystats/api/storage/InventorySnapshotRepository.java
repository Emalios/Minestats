package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Snapshot;

import java.util.List;

public interface InventorySnapshotRepository {

    void addSnapshot(Snapshot snapshot);

    List<Snapshot> findAllByInventory(Inventory inventory);

    /**
     * test uses
     */
    List<Snapshot> findAllByInventoryId(int inventoryId);


    /**
     * Returns the number last (greatest timestamp) snapshots. Ordered chronically by timestamp (from inferior to superior)
     * @param inventory
     * @param number limit of snapshot to get
     * @return sorted newest snapshots limited by "number"
     */
    List<Snapshot> findLastByInventory(Inventory inventory, int number);

}
