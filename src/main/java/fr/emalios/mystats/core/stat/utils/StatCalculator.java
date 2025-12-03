package fr.emalios.mystats.core.stat.utils;

import fr.emalios.mystats.core.dao.InventorySnapshotDao;
import fr.emalios.mystats.core.dao.PlayerInventoryDao;
import fr.emalios.mystats.core.dao.SnapshotItemDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.stat.Record;
import fr.emalios.mystats.core.stat.RecordType;
import fr.emalios.mystats.helper.Utils;
import oshi.util.tuples.Pair;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatCalculator {

    private static StatCalculator instance = new StatCalculator();

    private final PlayerInventoryDao playerInventoryDao = Database.getInstance().getPlayerInventoryDao();
    private final InventorySnapshotDao inventorySnapshotDao = Database.getInstance().getInventorySnapshotDao();
    private final SnapshotItemDao snapshotItemDao = Database.getInstance().getSnapshotItemDao();

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
    public Map<String, Pair<Double, RecordType>> genPerSecond(String playerName) {
        //don't know if there is a better option
        Map<String, RecordType> contentTypes = new HashMap<>();
        Map<String, Pair<Double, RecordType>> results = new HashMap<>();
        try {
            var inventoriesIds = this.playerInventoryDao.findInventoryIds(playerName);
            for (int invId : inventoriesIds) {
                var snapshots = this.inventorySnapshotDao.findAllByInvId(invId);
                Map<Long, List<fr.emalios.mystats.core.stat.Record>> history = new HashMap<>();
                for (var snapshot : snapshots) {
                    int snapshotId = snapshot.id();
                    var snapshotItems = this.snapshotItemDao.findBySnapshotId(snapshotId);
                    this.registerContentTypes(contentTypes, snapshotItems);
                    history.put(snapshot.timestamp(), snapshotItems);
                }
                Utils.makeStats(history).forEach((itemName, value) -> {
                    var result = new Pair<>(value, contentTypes.get(itemName));
                    results.merge(itemName, result, (p1, p2) -> new Pair<>(p1.getA() + p2.getA(), p1.getB()));
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private void registerContentTypes(Map<String, RecordType> contentTypes, List<Record> snapshotContent) {
        snapshotContent.forEach(snapshot -> contentTypes.put(snapshot.getResourceId(), snapshot.getType()));
    }

}
