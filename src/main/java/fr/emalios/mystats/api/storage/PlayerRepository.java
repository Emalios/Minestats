package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;

public interface PlayerRepository {

    boolean hasInventory(Inventory inventory);

    void save(StatPlayer statPlayer);

    StatPlayer getOrCreate(String name);

    StatPlayer findByName(String name);
}
