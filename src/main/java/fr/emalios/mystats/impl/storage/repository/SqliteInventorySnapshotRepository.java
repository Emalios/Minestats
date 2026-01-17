package fr.emalios.mystats.impl.storage.repository;

import fr.emalios.mystats.api.models.Inventory;
import fr.emalios.mystats.api.models.Record;
import fr.emalios.mystats.api.models.Snapshot;
import fr.emalios.mystats.api.storage.InventorySnapshotRepository;
import fr.emalios.mystats.impl.storage.dao.InventorySnapshotDao;
import fr.emalios.mystats.impl.storage.dao.RecordDao;

import java.sql.SQLException;
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
    public List<Snapshot> findAllByInventory(Inventory inventory) {
        return this.findAllByInventoryId(inventory.getId());
    }

    @Override
    public List<Snapshot> findAllByInventoryId(int inventoryId) {
        try {
            var snapshotRecords = this.snapshotDao.findAllByInvId(inventoryId);
            List<Snapshot> snapshots = new ArrayList<>();

            for (var snap : snapshotRecords) {
                Collection<Record> records = this.recordDao.findBySnapshotId(snap.id());
                snapshots.add(new Snapshot(inventoryId, records, snap.timestamp()));
            }
            return snapshots;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Snapshot> findLastByInventory(Inventory inventory, int number) {
        try {
            var snapshotRecords = this.snapshotDao.findLastByInvId(inventory.getId(), number);
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
