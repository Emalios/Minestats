package fr.emalios.minestats.api.storage;

import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.StatPlayer;

import java.util.Collection;

public interface PlayerInventoryRepository {

    void addInventory(StatPlayer statPlayer, Inventory inventory);

    boolean removeInventory(StatPlayer statPlayer, Inventory inventory);

    boolean hasInventory(StatPlayer statPlayer, Inventory inventory);

    Collection<Inventory> findByPlayer(StatPlayer statPlayer);

    void hydrate(StatPlayer statPlayer);
}
