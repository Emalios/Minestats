package fr.emalios.mystats.api.models.inventory;

import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.storage.Persistable;

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
     * Scan content contained in every valid IHandler and create an associated snapshot that is not persisted
     * @return snapshot with the content of the valid iihandlers of the inventory
     * <!> Inventory must exist in storage implementation </!>
     */
    public Snapshot createSnapshot() {
        Collection<Record> records = this.getHandlers().stream()
                .flatMap(iHandler -> iHandler.getContent().stream())
                .collect(Collectors.toSet());
        return new Snapshot(this.getId(), records);
    }

    /**
     * Test if the inventory is still valid by testing is IHandler
     * @return true if it has at least one valid IHandler, else false
     */
    public boolean hasHandlers() {
        return !this.getHandlers().isEmpty();
    }

    public void addHandler(IHandler handler) {
        this.handlers.add(handler);
    }

    public void addHandlers(Collection<IHandler> handlers) {
        handlers.forEach(this::addHandler);
    }

    /**
     * Get all existing handlers present in the inventory, filtered by IHandler::exists
     * @return existing inventory's handlers
     */
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

    public boolean containsPosition(Position position) {
        return this.invPositions.contains(position);
    }

    public Set<Position> getInvPositions() {
        return invPositions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(handlers, inventory.handlers) && Objects.equals(invPositions, inventory.invPositions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handlers, invPositions);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "invPositions=" + invPositions +
                ", nb handlers=" + handlers.size() +
                '}';
    }
}
