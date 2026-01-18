package fr.emalios.mystats.api;

import fr.emalios.mystats.api.models.inventory.IHandlerLoader;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.services.StatCalculatorService;
import fr.emalios.mystats.api.services.StatPlayerService;
import fr.emalios.mystats.api.storage.*;

public class StatsAPI {

    private static StatsAPI instance;

    private StatPlayerService playerService;
    private InventoryService inventoryService;
    private StatCalculatorService statCalculatorService;

    private StatsAPI() { }

    public static StatsAPI getInstance() {
        if (instance == null) {
            instance = new StatsAPI();
        }
        return instance;
    }

    public void init(PlayerRepository playerRepository,
                     InventoryRepository inventoryRepository,
                     PlayerInventoryRepository playerInventoryRepository,
                     InventorySnapshotRepository inventorySnapshotRepository,
                     InventoryPositionsRepository inventoryPositionsRepository,
                     IHandlerLoader iHandlerLoader
    ) {
        this.inventoryService = new InventoryService(inventoryRepository, playerInventoryRepository, inventorySnapshotRepository, iHandlerLoader, inventoryPositionsRepository);
        this.playerService = new StatPlayerService(playerRepository, playerInventoryRepository);
        this.statCalculatorService = new StatCalculatorService(inventoryService);
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public StatPlayerService getPlayerService() {
        return playerService;
    }


    public StatCalculatorService getStatCalculatorService() {
        return statCalculatorService;
    }
}
