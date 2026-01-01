package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;

public interface InventoryRepository {

    Inventory getById(int id);

}
