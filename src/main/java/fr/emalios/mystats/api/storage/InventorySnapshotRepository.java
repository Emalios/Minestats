package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Snapshot;

import java.util.Collection;

public interface InventorySnapshotRepository {

    void addSnapshot(Snapshot snapshot);

    Collection<Snapshot> findByInventory(Inventory inventory);

}
