package fr.emalios.mystats.api;

import java.time.Instant;
import java.util.Collection;

public class Snapshot {


    private final Collection<Record> content;
    private final int inventoryId;
    private final long timestamp;

    public Snapshot(int inventoryId, Collection<Record> content) {
        this.timestamp = Instant.now().getEpochSecond();
        this.content = content;
        this.inventoryId = inventoryId;
    }

    public Collection<Record> getContent() {
        return content;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
