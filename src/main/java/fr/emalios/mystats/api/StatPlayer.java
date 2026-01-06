package fr.emalios.mystats.api;

import fr.emalios.mystats.api.storage.Persistable;
import fr.emalios.mystats.api.storage.Storage;

import java.util.Collection;
import java.util.Objects;

public class StatPlayer extends Persistable {

    private final String name;

    public StatPlayer(String name) {
        this.name = name;
    }

    public void addInventory(Inventory inventory) {
        Storage.playerInventories().addInventory(this, inventory);
    }

    public boolean removeInventory(Inventory inventory) {
        return Storage.playerInventories().removeInventory(this, inventory);
    }

    public boolean hasInventory(Inventory inventory) {
        return Storage.playerInventories().hasInventory(this, inventory);
    }

    public Collection<Inventory> getInventories() {
        return Storage.playerInventories().findByPlayer(this);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StatPlayer that = (StatPlayer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
