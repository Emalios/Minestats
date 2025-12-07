package fr.emalios.mystats.api.stat;

import fr.emalios.mystats.api.storage.IStorage;

import java.util.Collection;

/**
 * Represent something that contains things to monitor
 */
public interface IHandler extends IStorage {

    boolean exists();
    Collection<Record> getContent();

}

