package fr.emalios.mystats.core.db;

import fr.emalios.mystats.core.stat.Record;

public class DatabaseWorker {

    public static void submitBatch(Record[] entries) {
        if (entries == null || entries.length == 0) return;

        Database.getInstance().executeWriteAsync(conn -> {
            try (var ps = conn.prepareStatement(
                    "INSERT INTO snapshot_items(snapshot_id, item_name, count, stat_type, countUnit) VALUES (?,?,?,?,?)"
            )) {
                /*
                for (Record r : entries) {
                    ps.setInt(1, r.snapshotId());
                    ps.setString(2, r.itemName());
                    ps.setDouble(3, r.count());
                    ps.setString(4, r.statType());
                    ps.setString(5, r.unit());
                    ps.addBatch();
                }
                ps.executeBatch();

                 */
            }
        });
    }
}
