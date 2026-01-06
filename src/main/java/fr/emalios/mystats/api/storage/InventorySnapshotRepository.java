package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Snapshot;

import java.util.Collection;
import java.util.List;

public interface InventorySnapshotRepository {

    void addSnapshot(Snapshot snapshot);

    List<Snapshot> findByInventory(Inventory inventory);

}
