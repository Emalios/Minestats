package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;

import java.util.Collection;

public interface PlayerInventoryRepository {

    void addInventory(StatPlayer statPlayer, Inventory inventory);

    boolean removeInventory(StatPlayer statPlayer, Inventory inventory);

    boolean hasInventory(StatPlayer statPlayer, Inventory inventory);

    Collection<Inventory> findByPlayer(StatPlayer statPlayer);
}
