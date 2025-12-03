package fr.emalios.mystats.core.stat;


import java.util.Objects;

/**
 * Represent a 'thing' contained in an inventory
 */

public class Record {

    private final RecordType type;
    private final String resourceId;
    private final float count;
    private final CountUnit countUnit;

    public Record(
            RecordType type,
            String resourceId,
            float count,
            CountUnit countUnit
    ) {
        this.type = Objects.requireNonNull(type);
        this.resourceId = Objects.requireNonNull(resourceId);
        this.count = count;
        this.countUnit = Objects.requireNonNull(countUnit);
    }

    // --- Getters ---
    public RecordType getType() { return type; }
    public String getResourceId() { return resourceId; }
    public float getCount() { return count; }
    public CountUnit getUnit() { return countUnit; }

    public Record mergeWith(Record record) {
        if(!this.resourceId.equals(record.getResourceId())) return this; //might throw an exception
        assert this.countUnit == record.getUnit();
        return new Record(this.type, this.resourceId, this.count + record.getCount(), this.countUnit);
    }

    // --- Utilitaires ---
    @Override
    public String toString() {
        return "Stat{" +
                "type=" + type +
                ", targetId='" + resourceId + '\'' +
                ", count=" + count +
                ", unit=" + countUnit +
                '}';
    }
}
