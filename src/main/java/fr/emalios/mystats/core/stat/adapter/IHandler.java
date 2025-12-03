package fr.emalios.mystats.core.stat.adapter;

import fr.emalios.mystats.core.stat.Record;

import java.util.Collection;

/**
 * Represent something that contains things to monitor
 */
public interface IHandler {

    boolean exists();
    Collection<Record> getContent();

}

