package fr.emalios.mystats.api;

import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.services.StatPlayerService;
import fr.emalios.mystats.api.storage.*;

public class StatsAPI {

    private static StatsAPI instance;

    private StatPlayerService playerService;
    private InventoryService inventoryService;

    private StatsAPI() { }

    public static StatsAPI getInstance() {
        if (instance == null) {
            instance = new StatsAPI();
        }
        return instance;
    }

    public void init(PlayerRepository playerRepository, InventoryRepository inventoryRepository,
                     PlayerInventoryRepository playerInventoryRepository, InventorySnapshotRepository inventorySnapshotRepository, InventoryPositionsRepository inventoryPositionsRepository) {
        this.inventoryService = new InventoryService(inventoryRepository, playerInventoryRepository, inventorySnapshotRepository, inventoryPositionsRepository);
        this.playerService = new StatPlayerService(playerRepository);
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public StatPlayerService getPlayerService() {
        return playerService;
    }



}
