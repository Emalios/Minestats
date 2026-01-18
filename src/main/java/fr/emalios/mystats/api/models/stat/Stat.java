package fr.emalios.mystats.api.models.stat;

import fr.emalios.mystats.api.models.record.Record;
import fr.emalios.mystats.api.models.record.CountUnit;
import fr.emalios.mystats.api.models.record.RecordType;

import java.util.Objects;

/**
 * Represent an evolution of a record in a given time unit
 */
public class Stat extends Record {

    private final TimeUnit timeUnit;

    public Stat(RecordType type, String resourceId, float count, CountUnit countUnit, TimeUnit timeUnit) {
        super(type, resourceId, count, countUnit);
        this.timeUnit = timeUnit;
    }

    public Stat(Record record, TimeUnit timeUnit) {
        super(record.getType(), record.getResourceId(), record.getCount(), record.getUnit());
        this.timeUnit = timeUnit;
    }

    public Stat mergeWith(Stat other) {
        if(this.timeUnit != other.timeUnit) throw new IllegalArgumentException("Trying to merge " + this.timeUnit + " with " + other.timeUnit);
        return new Stat(
                super.mergeWith(other),
                this.timeUnit
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Stat stat = (Stat) o;
        return timeUnit == stat.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timeUnit);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}

