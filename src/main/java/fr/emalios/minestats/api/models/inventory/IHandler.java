package fr.emalios.minestats.api.models.inventory;

import fr.emalios.minestats.api.models.record.Record;

import java.util.Collection;

/**
 * Represent something that contains things to monitor
 */
public interface IHandler {

    boolean exists();
    boolean hasChanged();
    Collection<Record> getContent();

}

