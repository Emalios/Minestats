package fr.emalios.mystats.api;

import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.storage.Persistable;
import fr.emalios.mystats.api.storage.Storage;

import java.util.*;
import java.util.stream.Collectors;

public class Inventory extends Persistable {

    private final List<IHandler> handlers = new ArrayList<>();
    private final Set<Position> invPositions = new HashSet<>();

    public Inventory() { }

    public Inventory(Set<Position> invPositions) {
        this.invPositions.addAll(invPositions);
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
        Snapshot snapshot = new Snapshot(this.getId(), records);
        Storage.inventorySnapshots().addSnapshot(snapshot);
    }

    public void recordContent(long timestamp) {
        Collection<Record> records = this.getHandlers().stream()
                .flatMap(iHandler -> iHandler.getContent().stream())
                .collect(Collectors.toSet());
        Snapshot snapshot = new Snapshot(this.getId(), records, timestamp);
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

    public void addPosition(Position position) {
        this.invPositions.add(position);
    }

    public void addPositions(Collection<Position> position) {
        position.forEach(this::addPosition);
    }

    public void removePosition(Position position) {
        this.invPositions.remove(position);
    }

    public Set<Position> getInvPositions() {
        return invPositions;
    }

    public Collection<Snapshot> getSnapshots(int limit) {
        return Storage.inventorySnapshots().findLastByInventory(this, limit);
    }

    public Collection<Snapshot> getAllSnapshots() {
        return Storage.inventorySnapshots().findAllByInventory(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(invPositions, inventory.invPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invPositions);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "invPositions=" + invPositions +
                ", nb handlers=" + handlers.size() +
                '}';
    }
}
