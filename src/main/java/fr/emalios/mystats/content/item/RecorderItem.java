package fr.emalios.mystats.content.item;

import fr.emalios.mystats.core.dao.InventoryDao;
import fr.emalios.mystats.core.dao.SnapshotItemDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/**
 * A click with this item on a block that has an inventory will monitor his content
 * If the block is already registered, it stops the monitoring
 */
public class RecorderItem extends Item {

    public RecorderItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Database database = Database.getInstance();

        //We only want server side logic
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return InteractionResult.FAIL;

        BlockState state = level.getBlockState(pos);

        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                pos,
                state,
                be,
                null // contexte / side : null si pas nécessaire
        );
        if (handler == null) return InteractionResult.PASS;
        try {
            //check if it's already monitored
            var inventoryRecord = database.getInventoryDao().findByBlockId(
                    context.getLevel().getDescription().getString(),
                    pos.getX(), pos.getY(), pos.getZ()
            );
            if(inventoryRecord.isEmpty()) {
                int playerId = database.getPlayerDao().insertIfNotExists(context.getPlayer().getName().getString());
                int invId = database.getInventoryDao().insert(
                        context.getLevel().getDescription().getString(),
                        pos.getX(), pos.getY(), pos.getZ(),
                        "ITEM");
                //start monitoring for the block

            } else {
                //logic to remove blocks from db ?
            }
            /*
            database.getPlayerInventoryDao().insert(playerId, invId);
            int snapshotId = database.getInventorySnapshotDao().insert(invId, Instant.now().getEpochSecond());
            SnapshotItemDao snapshotItemDao = database.getSnapshotItemDao();

            Map<String, Integer> content = Utils.getInventoryContent(handler);
            for (Map.Entry<String, Integer> entry : content.entrySet()) {
                String s = entry.getKey();
                Integer integer = entry.getValue();
                snapshotItemDao.insert(snapshotId, s, integer);
            }

             */

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return InteractionResult.SUCCESS;
    }

}
