package minestats.api.models;

import fr.emalios.mystats.api.models.inventory.Snapshot;
import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.inventory.IHandler;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.models.record.RecordType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Inventory's test")
public class InventoryTest {

    private final Position pos0 = new Position("pos", 0, 0, 0);
    private final Position pos64 = new Position("pos", 64, 0, 0);
    private final Position pos100 = new Position("pos", 100, 0, 0);
    private final Position pos1000 = new Position("pos", 1000, 0, 0);

    private final Record dirt1 = new Record(RecordType.ITEM, "minecraft:dirt", 1, CountUnit.ITEM);
    private final Record stone1 = new Record(RecordType.ITEM, "minecraft:stone", 1, CountUnit.ITEM);
    private final IHandler basicIhandler = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return List.of(dirt1, stone1);
        }
    };

    private final IHandler nonExisting = new IHandler() {
        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public Collection<Record> getContent() {
            return List.of(dirt1, stone1);
        }
    };

    private final IHandler emptyHandler = new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return List.of();
        }
    };

    //used by no one
    private final Position nullPos = new Position("null-pos", 1000, 0, 0);
    private final IHandler nullIHandler = new IHandler() {
        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public Collection<Record> getContent() {
            return List.of();
        }
    };

    @Test
    @DisplayName("null inventory")
    public void nullInventory() {
        Inventory inventory = new Inventory();
        Assertions.assertTrue(inventory.getInvPositions().isEmpty());
        Assertions.assertFalse(inventory.hasHandlers());
    }

    @Test
    @DisplayName("inventory has positions")
    public void inventoryHasPositions() {
        Set<Position> positions = Set.of(pos0, pos64, pos100);
        Inventory inventory = new Inventory(positions);

        Assertions.assertFalse(inventory.getInvPositions().isEmpty());
        Assertions.assertEquals(positions, inventory.getInvPositions());
        Assertions.assertFalse(inventory.hasHandlers());
        for (Position position : positions) {
            Assertions.assertTrue(inventory.containsPosition(position));
        }
        Assertions.assertFalse(inventory.containsPosition(nullPos));
    }

    @Test
    @DisplayName("inventory add positions by one")
    public void inventoryAddPositionsByOne() {
        Set<Position> positions = Set.of(pos0, pos64, pos100);
        Inventory inventory = new Inventory();
        positions.forEach(inventory::addPosition);

        Assertions.assertFalse(inventory.getInvPositions().isEmpty());
        Assertions.assertEquals(positions, inventory.getInvPositions());
        Assertions.assertFalse(inventory.hasHandlers());
        for (Position position : positions) {
            Assertions.assertTrue(inventory.containsPosition(position));
        }
        Assertions.assertFalse(inventory.containsPosition(nullPos));
    }

    @Test
    @DisplayName("inventory add positions by list")
    public void inventoryAddPositionsByList() {
        Set<Position> positions = Set.of(pos0, pos64, pos100);
        Inventory inventory = new Inventory();
        inventory.addPositions(positions);

        Assertions.assertFalse(inventory.getInvPositions().isEmpty());
        Assertions.assertEquals(positions, inventory.getInvPositions());
        Assertions.assertFalse(inventory.hasHandlers());
        for (Position position : positions) {
            Assertions.assertTrue(inventory.containsPosition(position));
        }
        Assertions.assertFalse(inventory.containsPosition(nullPos));
    }

    @Test
    @DisplayName("inventory remove position")
    public void inventoryRemovePosition() {
        Set<Position> positions = Set.of(pos0, pos64, pos100);
        Inventory inventory = new Inventory(positions);

        inventory.removePosition(pos0);
        Assertions.assertFalse(inventory.containsPosition(pos0));
        Assertions.assertTrue(inventory.containsPosition(pos64));
        Assertions.assertTrue(inventory.containsPosition(pos100));
        Assertions.assertEquals(2, inventory.getInvPositions().size());

        inventory.removePosition(pos64);
        Assertions.assertFalse(inventory.containsPosition(pos0));
        Assertions.assertFalse(inventory.containsPosition(pos64));
        Assertions.assertTrue(inventory.containsPosition(pos100));
        Assertions.assertEquals(1, inventory.getInvPositions().size());

        inventory.removePosition(pos100);
        Assertions.assertFalse(inventory.containsPosition(pos0));
        Assertions.assertFalse(inventory.containsPosition(pos64));
        Assertions.assertFalse(inventory.containsPosition(pos100));
        Assertions.assertTrue(inventory.getInvPositions().isEmpty());
    }

    @Test
    @DisplayName("inventory has handlers")
    public void inventoryHasHandlers() {
        Inventory inventory = new Inventory();
        inventory.addHandler(basicIhandler);

        Assertions.assertTrue(inventory.hasHandlers());
        Assertions.assertTrue(inventory.getHandlers().contains(basicIhandler));
        Assertions.assertFalse(inventory.getHandlers().contains(nullIHandler));

        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler, emptyHandler));

        Assertions.assertTrue(inv2.hasHandlers());
        Assertions.assertTrue(inv2.getHandlers().contains(basicIhandler));
        Assertions.assertTrue(inv2.getHandlers().contains(emptyHandler));
        Assertions.assertFalse(inv2.getHandlers().contains(nullIHandler));
    }

    @Test
    @DisplayName("get only existing handlers")
    public void getOnlyExistingHandlers() {
        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler, emptyHandler, nonExisting));

        Assertions.assertTrue(inv2.hasHandlers());
        Assertions.assertTrue(inv2.getHandlers().contains(basicIhandler));
        Assertions.assertTrue(inv2.getHandlers().contains(emptyHandler));
        Assertions.assertFalse(inv2.getHandlers().contains(nonExisting));
        Assertions.assertFalse(inv2.getHandlers().contains(nullIHandler));
    }

    @Test
    @DisplayName("create non persisted inventory snapshot")
    public void createNonPersistedInventorySnapshot() {
        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler));

        Assertions.assertThrows(IllegalStateException.class, () -> inv2.createSnapshot());
    }

    @Test
    @DisplayName("create basic inventory snapshot")
    public void createBasicInventorySnapshot() {
        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler));
        //fake persist for test purpose
        inv2.assignId(1);

        Snapshot snapshot = inv2.createSnapshot();
        Assertions.assertEquals(1, snapshot.getInventoryId());
        assertSameSnapshotContent(snapshot, List.of(dirt1, stone1));
    }

    @Test
    @DisplayName("create basic inventory snapshot with merged records")
    public void createBasicInventorySnapshotWithMergedRecords() {
        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler, basicIhandler));
        //fake persist for test purpose
        inv2.assignId(1);

        Snapshot snapshot = inv2.createSnapshot();
        Assertions.assertEquals(1, snapshot.getInventoryId());
        assertSameSnapshotContent(snapshot, List.of(dirt1.mergeWith(dirt1), stone1.mergeWith(stone1)));
    }

    @Test
    @DisplayName("create basic inventory snapshot with non existing handlers")
    public void createBasicInventorySnapshotWithNonExistingHandlers() {
        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler, nonExisting));
        //fake persist for test purpose
        inv2.assignId(1);

        Snapshot snapshot = inv2.createSnapshot();
        Assertions.assertEquals(1, snapshot.getInventoryId());
        assertSameSnapshotContent(snapshot, List.of(dirt1, stone1));
    }

    @Test
    @DisplayName("create basic inventory snapshot with empty handlers")
    public void createBasicInventorySnapshotWithEmptyHandlers() {
        Inventory inv2 = new Inventory();
        inv2.addHandlers(List.of(basicIhandler, emptyHandler));
        //fake persist for test purpose
        inv2.assignId(1);

        Snapshot snapshot = inv2.createSnapshot();
        Assertions.assertEquals(1, snapshot.getInventoryId());
        assertSameSnapshotContent(snapshot, List.of(dirt1, stone1));
    }

    private void assertSameSnapshotContent(Snapshot snapshot, List<Record> expected) {
        var content = snapshot.getContent();
        Assertions.assertEquals(content.size(), expected.size());
        for (Record record : List.of(dirt1, stone1)) {
            Assertions.assertTrue(content.contains(record));
        }
    }

    @Test
    @DisplayName("inventory equals to")
    public void inventoryEquals() {
        Inventory inv1 = new Inventory(Set.of(pos0, pos64, pos100));
        Inventory inv2 = new Inventory(Set.of(pos0, pos64, pos100));
        Assertions.assertEquals(inv1, inv2);

        inv1.addHandler(basicIhandler);
        Assertions.assertNotEquals(inv1, inv2);

        inv2.addHandler(basicIhandler);
        Assertions.assertEquals(inv1, inv2);

        Inventory inv3 = new Inventory(Set.of(pos0, pos64, pos100));
        Inventory inv4 = new Inventory(Set.of(pos0, pos64));
        Assertions.assertNotEquals(inv3, inv4);
    }

}
