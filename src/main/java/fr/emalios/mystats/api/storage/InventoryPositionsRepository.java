package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Position;
import fr.emalios.mystats.api.Snapshot;

import java.util.List;
import java.util.Set;

public interface InventoryPositionsRepository {

    void addPosition(Inventory inventory, Position position);

    boolean removePosition(Inventory inventory, Position position);

    boolean hasPosition(Inventory inventory, Position position);

    Set<Position> findAllByInventory(Inventory inventory);

    /**
     * test uses
     */
    Set<Position> findAllByInventoryId(int inventoryId);


}
