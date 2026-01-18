package minestats.api.storage;

import fr.emalios.mystats.api.models.inventory.IHandler;
import fr.emalios.mystats.api.models.inventory.IHandlerLoader;
import fr.emalios.mystats.api.models.inventory.Position;
import fr.emalios.mystats.api.models.record.Record;

import java.util.Collection;
import java.util.List;

public class TestHandlerLoader implements IHandlerLoader {

    private final static List<IHandler> handlers = List.of(new IHandler() {
        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public Collection<Record> getContent() {
            return List.of();
        }
    });

    @Override
    public Collection<IHandler> loadHandlers(Position position) {
        return handlers;
    }
}
