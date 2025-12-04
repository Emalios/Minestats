package fr.emalios.mystats.core.stat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Represent an evolution of a record in time
 */
public class Stat extends Record {

    private TimeUnit timeUnit;

    public Stat(RecordType type, String resourceId, float count, CountUnit countUnit, TimeUnit timeUnit) {
        super(type, resourceId, count, countUnit);
        this.timeUnit = timeUnit;
    }

    public Stat(Record record, TimeUnit timeUnit) {
        super(record.getType(), record.getResourceId(), record.getCount(), record.getUnit());
        this.timeUnit = timeUnit;
    }

    public Stat merge(Stat other) {
        if(this.getType() != other.getType()) {
            System.out.println("Merge failed.");
            return this;
        }
        return new Stat(
                super.mergeWith(other),
                this.timeUnit
        );
    }

    @Override
    public Record mergeWith(Record record) {
        return super.mergeWith(record);
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}

