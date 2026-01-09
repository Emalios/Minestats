package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.api.Snapshot;
import fr.emalios.mystats.api.storage.InventorySnapshotRepository;
import fr.emalios.mystats.impl.storage.dao.InventorySnapshotDao;
import fr.emalios.mystats.impl.storage.dao.RecordDao;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqliteInventorySnapshotRepository
        implements InventorySnapshotRepository {

    private final InventorySnapshotDao snapshotDao;
    private final RecordDao recordDao;

    public SqliteInventorySnapshotRepository(
            InventorySnapshotDao snapshotDao,
            RecordDao recordDao
    ) {
        this.snapshotDao = snapshotDao;
        this.recordDao = recordDao;
    }

    @Override
    public void addSnapshot(Snapshot snapshot) {
        System.out.println("insert " + snapshot.getTimestamp());
        int snapshotId = this.snapshotDao.insert(
                snapshot.getInventoryId(),
                snapshot.getTimestamp()
        );

        if (snapshotId <= 0) {
            throw new IllegalStateException("Snapshot insertion failed");
        }

        try {
            this.recordDao.insert(snapshotId, snapshot.getContent());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Snapshot> findByInventory(Inventory inventory) {
        System.out.println("find by inventory " + inventory);
        try {
            var snapshotRecords = this.snapshotDao.findAllByInvId(inventory.getId());
            List<Snapshot> snapshots = new ArrayList<>();

            for (var snap : snapshotRecords) {
                Collection<Record> records = this.recordDao.findBySnapshotId(snap.id());
                snapshots.add(new Snapshot(inventory.getId(), records, snap.timestamp()));
            }
            return snapshots;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
