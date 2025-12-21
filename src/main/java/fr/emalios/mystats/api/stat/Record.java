package fr.emalios.mystats.api.stat;


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
        if(!this.type.equals(record.type)) {
            throw new IllegalArgumentException("Trying to merge " + this.type + " with " + record.type);
        }
        if(!this.resourceId.equals(record.getResourceId())) {
            throw new IllegalArgumentException("Trying to merge " + this.resourceId + " with " + record.resourceId);
        }
        if(!this.countUnit.equals(record.getUnit())) {
            throw new IllegalArgumentException("Trying to merge " + this.countUnit + " with " + record.countUnit);
        }
        return new Record(this.type, this.resourceId, this.count + record.getCount(), this.countUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Float.compare(count, record.count) == 0 && type == record.type && Objects.equals(resourceId, record.resourceId) && countUnit == record.countUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resourceId, count, countUnit);
    }

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
