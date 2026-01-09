package fr.emalios.mystats.api;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collection;

public class Snapshot implements Comparable<Snapshot> {


    private final Collection<Record> content;
    private final int inventoryId;
    private final long timestamp;

    public Snapshot(int inventoryId, Collection<Record> content) {
        this.timestamp = Instant.now().getEpochSecond();
        this.content = content;
        this.inventoryId = inventoryId;
    }

    public Snapshot(int inventoryId, Collection<Record> content, long timestamp) {
        this.inventoryId = inventoryId;
        this.content = content;
        this.timestamp = timestamp;
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

    @Override
    public int compareTo(@NotNull Snapshot o) {
        return Long.compare(timestamp, o.timestamp);
    }
}
