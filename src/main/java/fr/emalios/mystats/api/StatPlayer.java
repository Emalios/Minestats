package fr.emalios.mystats.api;

import fr.emalios.mystats.api.storage.Persistable;
import fr.emalios.mystats.api.storage.Storage;

import java.util.Collection;

public class StatPlayer extends Persistable {

    private final String name;

    public StatPlayer(String name) {
        this.name = name;
    }

    public void addInventory(Inventory inventory) {
        Storage.playerInventories().addInventory(this, inventory);
    }

    public void removeInventory(Inventory inventory) {
        Storage.playerInventories().removeInventory(this, inventory);
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

}
