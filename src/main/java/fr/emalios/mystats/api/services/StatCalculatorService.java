package fr.emalios.mystats.api.services;

import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.record.RecordType;
import fr.emalios.mystats.api.models.stat.Stat;
import fr.emalios.mystats.api.models.stat.TimeUnit;
import fr.emalios.mystats.helper.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatCalculatorService {

    private final InventoryService inventoryService;

    public StatCalculatorService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<Stat> genPerSecond(Collection<Inventory> inventories) {
        //don't know if there is a better option to merge record with same resourceId
        Map<String, RecordType> contentTypes = new HashMap<>();
        Map<String, Stat> results = new HashMap<>();
        for (Inventory inventory : inventories) {
            var snapshots = this.inventoryService.getLastSnapshots(inventory, 10);
            Map<Long, Collection<Record>> history = new HashMap<>();
            for (var snapshot : snapshots) {
                var content = snapshot.getContent();
                this.registerContentTypes(contentTypes, content);
                history.put(snapshot.getTimestamp(), content);
            }
            Utils.makeStats(history).forEach((itemName, value) -> {
                RecordType recordType = contentTypes.get(itemName);
                Stat stat = new Stat(
                        recordType,
                        itemName,
                        value,
                        switch (recordType) {
                            case ITEM -> CountUnit.ITEM;
                            case FLUID -> CountUnit.MB;
                        },
                        TimeUnit.SECOND
                );
                results.merge(itemName, stat, Stat::mergeWith);
            });
        }
        return results.values().stream().toList();
    }

    private void registerContentTypes(Map<String, RecordType> contentTypes, Collection<Record> snapshotContent) {
        snapshotContent.forEach(snapshot -> contentTypes.put(snapshot.getResourceId(), snapshot.getType()));
    }

}
