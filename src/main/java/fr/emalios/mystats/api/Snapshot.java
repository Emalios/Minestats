package fr.emalios.mystats.api;

import java.time.Instant;
import java.util.Collection;

public class Snapshot {


    private final Collection<Record> content;
    private final Inventory inventory;
    private final long timestamp;

    public Snapshot(Inventory inventory, Collection<Record> content) {
        this.timestamp = Instant.now().getEpochSecond();
        this.inventory = inventory;
        this.content = content;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Collection<Record> getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
