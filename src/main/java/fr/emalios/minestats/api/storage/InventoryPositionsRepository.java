package fr.emalios.minestats.api.storage;

import fr.emalios.minestats.api.models.inventory.IPosition;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;

import java.util.Set;

public interface InventoryPositionsRepository {

    void addPosition(Inventory inventory, IPosition position);

    boolean removePosition(Inventory inventory, IPosition position);

    boolean hasPosition(Inventory inventory, IPosition position);

    Set<IPosition> findAllByInventory(Inventory inventory);

    /**
     * test uses
     */
    Set<IPosition> findAllByInventoryId(int inventoryId);


}
