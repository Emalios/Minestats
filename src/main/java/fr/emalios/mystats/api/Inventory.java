package fr.emalios.mystats.api;

import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.storage.Persistable;
import fr.emalios.mystats.api.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Inventory extends Persistable {

    private final List<IHandler> handlers = new ArrayList<>();
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public Inventory(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
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

    public void addHandler(IHandler handler) {
        this.handlers.add(handler);
    }

    public Collection<IHandler> getHandlers() {
        return this.handlers.stream().filter(IHandler::exists).toList();
    }

    public Collection<Snapshot> getSnapshots() {
        return Storage.inventorySnapshots().findByInventory(this);
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
}
