package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;

import java.util.Collection;
import java.util.Optional;

public interface InventoryRepository {

    void save(Inventory inventory);

    /**
     * Method to find an inventory stored in the world at the coordinate.
     * Should be used when you're not sure the inventory exist
     * @param world
     * @param x
     * @param y
     * @param z
     * @return inventory if present or Optional.empty
     */
    Optional<Inventory> findByPos(String world, int x, int y, int z);

    /**
     * Should be use when you want either to get the existing inventory or create it.
     * In both case the inventory will be stored.
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    Inventory getOrCreate(String world, int x, int y, int z);

    void delete(Inventory inventory);

    Collection<Inventory> getAllFromWorld(String world);

    Collection<Inventory> getAll();

    Inventory getById(int id);

}
