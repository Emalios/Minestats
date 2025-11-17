package fr.emalios.mystats.core.stat;

import fr.emalios.mystats.core.dao.InventoryDao;
import fr.emalios.mystats.core.dao.InventorySnapshotDao;
import fr.emalios.mystats.core.dao.PlayerInventoryDao;
import fr.emalios.mystats.core.dao.SnapshotItemDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.network.chat.Component;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatCalculator {

    private static StatCalculator instance = new StatCalculator();

    private final StatManager statManager = StatManager.getInstance();
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
    public Map<String, Double> genPerSecond(String playerName) {
        Map<String, Double> result = new HashMap<>();
        try {
            var inventoriesIds = this.playerInventoryDao.findInventoryIds(playerName);
            for (int invId : inventoriesIds) {
                var snapshots = this.inventorySnapshotDao.findAllByInvId(invId);
                Map<Long, List<SnapshotItemDao.ItemRecord>> history = new HashMap<>();
                for (var snapshot : snapshots) {
                    int snapshotId = snapshot.id();
                    var snapshotItems = this.snapshotItemDao.findBySnapshotId(snapshotId);
                    history.put(snapshot.timestamp(), snapshotItems);
                }
                Utils.makeStats(history).forEach((itemName, value) -> {
                    if(result.containsKey(itemName)) {
                        result.put(itemName, result.get(itemName) + value);
                    } else {
                        result.put(itemName, value);
                    }
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
