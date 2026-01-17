package fr.emalios.mystats.api.stat.utils;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.CountUnit;
import fr.emalios.mystats.api.models.Inventory;
import fr.emalios.mystats.api.models.RecordType;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.stat.*;
import fr.emalios.mystats.api.models.Record;
import fr.emalios.mystats.helper.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatCalculator {

    //TODO make stat service
    private static StatCalculator instance = new StatCalculator();

    private StatsAPI statsAPI;

    private StatCalculator() {
        this.statsAPI = StatsAPI.getInstance();
    }

    public static synchronized StatCalculator getInstance() {
        if (instance == null) {
            instance = new StatCalculator();
        }
        return instance;
    }

    /**
     * Calculate for every monitored items in every inventories the delta per second
     * @return map of items id associated to their delta/s
     */
    public List<Stat> genPerSecond(Collection<Inventory> inventories) {
        //don't know if there is a better option to merge record with same resourceId
        Map<String, RecordType> contentTypes = new HashMap<>();
        Map<String, Stat> results = new HashMap<>();
        for (Inventory inventory : inventories) {
            var snapshots = this.statsAPI.getInventoryService().getLastSnapshots(inventory, 10);
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
