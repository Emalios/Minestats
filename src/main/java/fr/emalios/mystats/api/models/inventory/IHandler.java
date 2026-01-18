package fr.emalios.mystats.api.models.inventory;

import fr.emalios.mystats.api.models.record.Record;

import java.util.Collection;

/**
 * Represent something that contains things to monitor
 */
public interface IHandler {

    boolean exists();
    Collection<Record> getContent();

}

