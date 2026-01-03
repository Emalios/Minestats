package fr.emalios.mystats.api;

import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.storage.Persistable;
import fr.emalios.mystats.api.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Inventory extends Persistable {

    private final List<IHandler> handlers = new ArrayList<>();
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    private final String blockId;

    public Inventory(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockId = world + ":" + x + ":" + y + ":" + z;
    }

    /**
     * Scan content contained in every valid IHandler and create an associated snapshot
     * <!> Inventory must exist in storage implementation </!>
     */
    public void recordContent() {
        /*
        assuming every record in different handlers are different, we can simply merge list without
        trying to merge records with each other
        */
        Collection<Record> records = this.getHandlers().stream()
                .flatMap(iHandler -> iHandler.getContent().stream())
                .collect(Collectors.toSet());
        Snapshot snapshot = new Snapshot(this, records);
        Storage.inventorySnapshots().addSnapshot(snapshot);
    }

    /**
     * Test if the inventory is still valid by testing is IHandler
     * @return true if it has at least one valid IHandler, else false
     */
    public boolean isValid() {
        return !this.getHandlers().isEmpty();
    }

    public void addHandler(IHandler handler) {
        this.handlers.add(handler);
    }

    public void addHandlers(Collection<IHandler> handlers) {
        handlers.forEach(this::addHandler);
    }

    public Collection<IHandler> getHandlers() {
        return this.handlers.stream().filter(IHandler::exists).toList();
    }

    public Collection<Snapshot> getSnapshots() {
        return Storage.inventorySnapshots().findByInventory(this);
    }

    public String getBlockId() {
        return this.blockId;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(blockId, inventory.blockId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(blockId);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
