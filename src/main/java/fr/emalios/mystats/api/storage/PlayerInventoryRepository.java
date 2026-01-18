package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.StatPlayer;

import java.util.Collection;

public interface PlayerInventoryRepository {

    void addInventory(StatPlayer statPlayer, Inventory inventory);

    boolean removeInventory(StatPlayer statPlayer, Inventory inventory);

    boolean hasInventory(StatPlayer statPlayer, Inventory inventory);

    Collection<Inventory> findByPlayer(StatPlayer statPlayer);

    void hydrate(StatPlayer statPlayer);
}
