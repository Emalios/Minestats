package fr.emalios.mystats.core.dao;

import java.time.Instant;
import java.util.List;

public class InventorySnapshot {

    public final int inventoryId;
    public final Instant timestamp;
    public final List<ItemSnapshot> items;

    public InventorySnapshot(int inventoryId, Instant timestamp, List<ItemSnapshot> items) {
        this.inventoryId = inventoryId;
        this.timestamp = timestamp;
        this.items = items;
    }
}