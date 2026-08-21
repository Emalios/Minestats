package fr.emalios.minestats.api.models.inventory;

import java.util.Collection;

@FunctionalInterface
public interface IHandlerLoader {

    Collection<IHandler> loadHandlers(IPosition position);

}
