package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Position;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface InventoryRepository {

    void save(Inventory inventory);

    /**
     * Method to find an inventory stored in the world at the coordinate.
     * Should be used when you're not sure the inventory exist
     * @param position
     * @return inventory if present or Optional.empty
     */
    Optional<Inventory> findByPos(Position position);

    /**
     * Useful method that either:
     * - get the inventory that is associated with the given position
     * - create an inventory associated with the given position
     * In both case the returned inventory will be stored.
     * @param position position of the created inventory
     * @return persisted inventory
     */
    Inventory getOrCreate(Position position);

    void delete(Inventory inventory);

    Set<Inventory> getAllFromWorld(String world);

    Set<Inventory> getAll();

    Inventory getById(int id);

}
