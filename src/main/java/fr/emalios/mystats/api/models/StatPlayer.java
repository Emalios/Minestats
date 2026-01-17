package fr.emalios.mystats.api.models;

import fr.emalios.mystats.api.storage.Persistable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StatPlayer extends Persistable {

    private final String name;
    private final Set<Inventory> inventories = new HashSet<>();

    public StatPlayer(String name) {
        this.name = name;
    }

    public boolean hasInventory(Inventory inventory) {
        return this.inventories.contains(inventory);
    }

    public void addInventory(Inventory inventory) {
        this.inventories.add(inventory);
    }

    public boolean removeInventory(Inventory inventory) {
        return this.inventories.remove(inventory);
    }

    public Set<Inventory> getInventories() {
        return Collections.unmodifiableSet(inventories);
    }

    public String getName() {
        return name;
    }

    /**
     * Clear the current inventories of the player to update them
     * @param inventories updated inventories
     */
    public void loadInventories(Collection<Inventory> inventories) {
        this.inventories.clear();
        this.inventories.addAll(inventories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatPlayer that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
