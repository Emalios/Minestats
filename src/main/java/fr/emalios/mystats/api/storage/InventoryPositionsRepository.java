package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;

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
