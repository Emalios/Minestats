package fr.emalios.mystats.api.stat.utils;

import fr.emalios.mystats.impl.storage.dao.InventorySnapshotDao;
import fr.emalios.mystats.impl.storage.dao.PlayerInventoryDao;
import fr.emalios.mystats.impl.storage.dao.RecordDao;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.api.stat.*;
import fr.emalios.mystats.api.stat.Record;
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
    private final RecordDao recordDao = Database.getInstance().getSnapshotItemDao();

    private StatCalculator() {

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
    public Collection<Stat> genPerSecond(String playerName) {
        //don't know if there is a better option
        Map<String, RecordType> contentTypes = new HashMap<>();
        Map<String, Stat> results = new HashMap<>();
        try {
            var inventoriesIds = this.playerInventoryDao.findInventoryIds(playerName);
            for (int invId : inventoriesIds) {
                //TODO: bug ICI pas de nouvelle stat calculé, log bizarre
                var snapshots = this.inventorySnapshotDao.findAllByInvId(invId);
                Map<Long, List<Record>> history = new HashMap<>();
                for (var snapshot : snapshots) {
                    int snapshotId = snapshot.id();
                    var snapshotItems = this.recordDao.findBySnapshotId(snapshotId);
                    this.registerContentTypes(contentTypes, snapshotItems);
                    history.put(snapshot.timestamp(), snapshotItems);
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
                    results.merge(itemName, stat, Stat::merge);
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results.values();
    }

    private void registerContentTypes(Map<String, RecordType> contentTypes, List<Record> snapshotContent) {
        snapshotContent.forEach(snapshot -> contentTypes.put(snapshot.getResourceId(), snapshot.getType()));
    }

}
