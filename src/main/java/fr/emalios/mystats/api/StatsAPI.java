package fr.emalios.mystats.api;

import fr.emalios.mystats.api.models.inventory.IHandlerLoader;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.services.StatCalculatorService;
import fr.emalios.mystats.api.services.StatPlayerService;
import fr.emalios.mystats.api.storage.*;

public abstract class StatsAPI {

    private StatPlayerService playerService;
    private InventoryService inventoryService;
    private StatCalculatorService statCalculatorService;

    public void init() {
        this.onInit();
        this.inventoryService = new InventoryService(this.buildInventoryRepository(), this.buildPlayerInventoryRepository(), this.buildInventorySnapshotRepository(), this.getIHandlerLoader(), this.buildInventoryPositionsRepository());
        this.playerService = new StatPlayerService(this.buildPlayerRepository(), this.buildPlayerInventoryRepository(), this.inventoryService);
        this.statCalculatorService = new StatCalculatorService(inventoryService);
    }

    public void close() {
        this.onShutdown();
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

    public abstract PlayerRepository buildPlayerRepository();
    public abstract PlayerInventoryRepository buildPlayerInventoryRepository();
    public abstract InventorySnapshotRepository buildInventorySnapshotRepository();
    public abstract InventoryPositionsRepository buildInventoryPositionsRepository();
    public abstract InventoryRepository buildInventoryRepository();

    public abstract IHandlerLoader getIHandlerLoader();

    public abstract void onInit();
    public abstract void onShutdown();
}
