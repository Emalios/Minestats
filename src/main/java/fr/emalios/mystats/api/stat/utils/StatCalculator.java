package fr.emalios.mystats.api.stat.utils;

import fr.emalios.mystats.api.CountUnit;
import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.RecordType;
import fr.emalios.mystats.impl.storage.dao.InventorySnapshotDao;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;
import fr.emalios.mystats.impl.storage.dao.RecordDao;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.api.stat.*;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.helper.Utils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatCalculator {

    private static StatCalculator instance = new StatCalculator();

    private final PlayerInventoryDao playerInventoryDao = Database.getInstance().getPlayerInventoryDao();
    private final InventorySnapshotDao inventorySnapshotDao = Database.getInstance().getInventorySnapshotDao();
    private final RecordDao recordDao = Database.getInstance().getRecordDao();

    private StatCalculator() { }

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
            var snapshots = inventory.getSnapshots();
            System.out.println("snapshots: " + snapshots.size());
            Map<Long, Collection<Record>> history = new HashMap<>();
            for (var snapshot : snapshots) {
                var content = snapshot.getContent();
                this.registerContentTypes(contentTypes, content);
                System.out.println("current history: " + history);
                System.out.println("about to insert at key: " + snapshot.getTimestamp());
                history.put(snapshot.getTimestamp(), content);
            }
            System.out.println(history);
            Utils.makeStats(history).forEach((itemName, value) -> {
                System.out.println("itemName: " + itemName + ", value: " + value);
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
