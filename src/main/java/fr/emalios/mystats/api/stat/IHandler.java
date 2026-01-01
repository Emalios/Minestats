package fr.emalios.mystats.api.stat;

import fr.emalios.mystats.api.Record;

import java.util.Collection;

/**
 * Represent something that contains things to monitor
 */
public interface IHandler {

    boolean exists();
    Collection<Record> getContent();

}

