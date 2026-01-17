package fr.emalios.mystats.impl.adapter;

import fr.emalios.mystats.api.StatsAPI;
import fr.emalios.mystats.api.models.Inventory;
import fr.emalios.mystats.api.models.Position;
import fr.emalios.mystats.api.services.InventoryService;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.models.Record;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StatManager {

    private static StatManager instance;

    private final int BATCH_SIZE = 100;
    private final int FLUSH_INTERVAL_SECONDS = 5;

    //link an inventoryId to associated capability handlers
    //might be necessary to use coordinates/level here
    private final Set<Inventory> monitored = new HashSet<>();
    private final Queue<Record> buffer = new ConcurrentLinkedQueue<>();

    private InventoryService inventoryService;

    private StatManager() {
        this.inventoryService = StatsAPI.getInstance().getInventoryService();
    }

    public static synchronized StatManager getInstance() {
        if (instance == null) {
            instance = new StatManager();
        }
        return instance;
    }

    public void reset() {
        this.monitored.clear();
    }

    public void monitore(Inventory inventory) {
        this.monitored.add(inventory);
    }

    public boolean isMonitored(Inventory inventory) {
        return this.monitored.contains(inventory);
    }



    public void unmonitore(Inventory inventory) {
        this.monitored.remove(inventory);
        //Storage.inventories().delete(inventory);
    }

    //load inventories in database
    public void init(MinecraftServer server) throws SQLException {
        this.reset();
        Map<String, ServerLevel> levels = new HashMap<>();
        server.getAllLevels().forEach(level -> {
            levels.put(level.dimension().location().toString(), level);
        });
        //TODO: maybe logic to test if every handlers are the same here
        for (Inventory inventory : this.inventoryService.getAll()) {
            for (Position invPosition : inventory.getInvPositions()) {
                BlockPos pos = new BlockPos(invPosition.getX(), invPosition.getY(), invPosition.getZ());
                Level level = levels.get(invPosition.getWorld());
                //load block in world to be able to get capabilities
                level.getBlockState(pos);
                List<IHandler> handlers = Utils.getIHandlers(level, pos);
                //no handlers detected on the position
                if(handlers.isEmpty()) {
                    this.inventoryService.removePositionFromInventory(inventory, invPosition);
                } else inventory.addHandlers(handlers);
            }
            if(inventory.isValid()) this.monitore(inventory);
            //if there is no available handlers delete the block
            else this.inventoryService.deleteInventory(inventory);
        }
    }
}
