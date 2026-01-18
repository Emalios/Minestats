package fr.emalios.mystats.api.services;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.inventory.Inventory;
import fr.emalios.mystats.api.models.StatPlayer;
import fr.emalios.mystats.api.storage.PlayerInventoryRepository;
import fr.emalios.mystats.api.storage.PlayerRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StatPlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerInventoryRepository playerInventoryRepository;

    private final Map<String, StatPlayer> loadedPlayers = new HashMap<>();

    public StatPlayerService(PlayerRepository playerRepository, PlayerInventoryRepository playerInventoryRepository) {
        this.playerRepository = playerRepository;
        this.playerInventoryRepository = playerInventoryRepository;
    }

    public StatPlayer getOrCreateByName(String playerName) {
        if(this.loadedPlayers.containsKey(playerName)) return this.loadedPlayers.get(playerName);

        StatPlayer statPlayer = this.playerRepository.getOrCreate(playerName);
        this.loadPlayer(playerName, statPlayer);
        return statPlayer;
    }

    public boolean isLoaded(String playerName) {
        return this.loadedPlayers.containsKey(playerName);
    }

    public Optional<StatPlayer> findByName(String playerName) {
        if(this.loadedPlayers.containsKey(playerName)) return Optional.of(this.loadedPlayers.get(playerName));

        Optional<StatPlayer> optPlayer = this.playerRepository.findByName(playerName);
        if(optPlayer.isPresent()) {
            StatPlayer statPlayer = optPlayer.get();
            this.loadPlayer(playerName, statPlayer);
            return Optional.of(statPlayer);
        }
        return Optional.empty();
    }

    private void loadPlayer(String playerName, StatPlayer statPlayer) {
        this.loadedPlayers.put(playerName, statPlayer);
        StatsAPI.getInstance().getInventoryService().loadInventories(statPlayer);
    }

    public void addInventoryToPlayer(StatPlayer statPlayer, Inventory inventory) {
        this.playerInventoryRepository.addInventory(statPlayer, inventory);
        statPlayer.addInventory(inventory);
    }

    public boolean removeInventoryFromPlayer(StatPlayer statPlayer, Inventory inventory) {
        return this.playerInventoryRepository.removeInventory(statPlayer, inventory);
    }

}
