package fr.emalios.mystats.api.storage;

import fr.emalios.mystats.api.models.StatPlayer;

import java.util.Optional;

public interface PlayerRepository {

    void save(StatPlayer statPlayer);

    StatPlayer getOrCreate(String name);

    Optional<StatPlayer> findByName(String name);
}
