package fr.emalios.mystats.api.models.inventory;

import java.util.Collection;

@FunctionalInterface
public interface IHandlerLoader {

    Collection<IHandler> loadHandlers(Position position);

}
