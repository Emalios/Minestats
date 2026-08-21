package fr.emalios.minestats.api.models.inventory;

import fr.emalios.minestats.api.models.record.Record;
import fr.emalios.minestats.api.storage.Persistable;

import java.util.*;
import java.util.stream.Collectors;

public class Inventory extends Persistable {

    private List<IHandler> handlers = new ArrayList<>();
    private final Set<IPosition> invPositions = new HashSet<>();

    public Inventory() { }

    public Inventory(Set<IPosition> invPositions) {
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
     * Test if the inventory is still valid, to be valid all handlers needs to be existing and needs to not have changed
     * @return true if all handlers are existing and their capability has not changed
     */
    public boolean isValid() {
        for (IHandler handler : this.handlers) {
            if (!handler.exists() || handler.hasChanged()) return false;
        }
        return true;
    }

    /**
     * Test if the inventory has at least one handler.
     * @return true if it has at least one valid IHandler, else false
     */
    public boolean hasHandlers() {
        return !this.handlers.stream().filter(IHandler::exists).toList().isEmpty();
    }

    //TODO: throw error if handler does not exists
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

    public void addPosition(IPosition position) {
        this.invPositions.add(position);
    }

    public void addPositions(Collection<IPosition> position) {
        position.forEach(this::addPosition);
    }

    public void removePosition(Position position) {
        this.invPositions.remove(position);
    }

    public boolean containsPosition(Position position) {
        return this.invPositions.contains(position);
    }

    public Set<IPosition> getInvPositions() {
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
                ", handlers=" + handlers +
                ", nb handlers=" + handlers.size() +
                '}';
    }
}
