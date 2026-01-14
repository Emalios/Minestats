package fr.emalios.mystats.impl.adapter;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.Position;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.api.Record;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.dao.InventoryDao;
import fr.emalios.mystats.impl.storage.dao.InventorySnapshotDao;
import fr.emalios.mystats.impl.storage.dao.RecordDao;
import fr.emalios.mystats.impl.storage.db.Database;
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

    private StatManager() {
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

    /**
     * TODO: maybe a system to detect if the inventory has changed since last scan is possible
     * Method responsible to scan the content of every monitored blocks
     * for each block we:
     * - create an inventory snapshot
     * - scan inventory content
     * - for each item create a snapshot item
     */
    public void scan() {
        for (Inventory inventory : this.monitored) {
            if(inventory.isValid()) inventory.recordContent();
            else Storage.inventories().delete(inventory);
        }
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
        for (Inventory inventory : Storage.inventories().getAll()) {
            for (Position invPosition : inventory.getInvPositions()) {
                BlockPos pos = new BlockPos(invPosition.getX(), invPosition.getY(), invPosition.getZ());
                Level level = levels.get(invPosition.getWorld());
                //load block in world to be able to get capabilities
                level.getBlockState(pos);
                //MyStats.LOGGER.debug("State {}", state);
                List<IHandler> handlers = Utils.getIHandlers(level, pos);
                MyStats.LOGGER.debug("Looking at {} at {} ", invPosition, invPosition.getWorld());
                MyStats.LOGGER.debug("Handlers: {}", handlers);
                //no handlers detected on the position
                if(handlers.isEmpty()) {
                    MyStats.LOGGER.debug("Deleting inventory {}", inventory);
                    Storage.inventoryPositions().removePosition(inventory, invPosition);
                }
                inventory.addHandlers(handlers);
            }
            if(inventory.isValid()) this.monitore(inventory);
            //if there is no available handlers delete the block
            else Storage.inventories().delete(inventory);
        }
    }
}
