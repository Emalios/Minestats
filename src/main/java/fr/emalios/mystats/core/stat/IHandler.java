package fr.emalios.mystats.core.stat;

import java.util.Collection;

/**
 * Represent something that contains things to monitor
 */
public interface IHandler {

    boolean exists();
    Collection<Stat> getContent();

}

