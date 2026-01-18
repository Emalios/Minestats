package fr.emalios.mystats.api.models.inventory;

import fr.emalios.mystats.api.models.record.Record;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

public class Snapshot implements Comparable<Snapshot> {


    private final Collection<fr.emalios.mystats.api.models.record.Record> content;
    private final int inventoryId;
    private final long timestamp;

    public Snapshot(int inventoryId, Collection<fr.emalios.mystats.api.models.record.Record> content) {
        this.timestamp = Instant.now().getEpochSecond();
        this.content = content;
        this.inventoryId = inventoryId;
    }

    public Snapshot(int inventoryId, Collection<fr.emalios.mystats.api.models.record.Record> content, long timestamp) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Snapshot snapshot = (Snapshot) o;
        return inventoryId == snapshot.inventoryId && timestamp == snapshot.timestamp && Objects.equals(content, snapshot.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, inventoryId, timestamp);
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                ", timestamp=" + timestamp +
                '}';
    }
}
