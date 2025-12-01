package fr.emalios.mystats.content.item;

import fr.emalios.mystats.MyStats;
import fr.emalios.mystats.content.menu.MonitorMenu;
import fr.emalios.mystats.core.dao.InventoryDao;
import fr.emalios.mystats.core.dao.SnapshotItemDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.stat.StatManager;
import fr.emalios.mystats.helper.Utils;
import fr.emalios.mystats.registries.StatDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

/**
 * Main item of the mod used to add and remove monitored blocks. Also used to show the stats for a clicked block
 * The behavior of this item is determined by his mode (RecorderMode) either ADD, REMOVE or VIEW
 */
public class RecorderItem extends Item {

    private final Database database = Database.getInstance();
    private final StatManager statManager = StatManager.getInstance();

    public RecorderItem(Properties props) {
        super(props
                .stacksTo(1)
                .component(StatDataComponent.RECORDER_COMPONENT.value(), new RecorderDataComponent.RecorderData(RecorderDataComponent.RecorderMode.ADD)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Ne rien faire côté client (optionnel mais propre)
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }

        // Raytrace pour savoir si le joueur vise un bloc
        // distance 5.0D = portée standard d'interaction main droite
        HitResult hit = player.pick(5.0D, 0.0F, false);

        // Si on vise un bloc, on ne gère rien ici : laisser useOn() faire le boulot
        if (hit.getType() == HitResult.Type.BLOCK || player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        // Sinon (on vise l'air) : gérer le changement de mode
        var mode = stack.get(StatDataComponent.RECORDER_COMPONENT).mode();
        if (!player.isShiftKeyDown()) {
            mode = mode.next();
            stack.set(StatDataComponent.RECORDER_COMPONENT, new RecorderDataComponent.RecorderData(mode));
            player.sendSystemMessage(Component.literal("Set to '" + mode + "' mode."));
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide()) return InteractionResult.PASS;

        ItemStack itemStack = context.getItemInHand();
        var mode = itemStack.get(StatDataComponent.RECORDER_COMPONENT).mode();
        Player player = context.getPlayer();
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            //if not shift click, new config
            if(!player.isShiftKeyDown()) {
                mode = mode.next();
                player.sendSystemMessage(Component.literal("Set to '" + mode + "' mode."));
                itemStack.set(StatDataComponent.RECORDER_COMPONENT, new RecorderDataComponent.RecorderData(mode));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(pos);

        //TODO: https://docs.neoforged.net/docs/1.21.1/datastorage/capabilities/#block-capability-caching

        IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK,
                pos,
                state,
                be,
                null // contexte / side : null si pas nécessaire
        );
        if (handler == null) return InteractionResult.PASS;

        //create cache
        var capCache = BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK, // capability to cache
                (ServerLevel) level, // level
                pos, // target position
                null // context
        );

        try {
            return this.processClick(mode, player, level, capCache, pos);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<InventoryDao.InventoryRecord> getRecord(Level level, BlockPos pos) throws SQLException {
        var inventoryRecord = database.getInventoryDao().findByBlockId(
                level.getDescription().getString(),
                pos.getX(), pos.getY(), pos.getZ()
        );
        return inventoryRecord;
    }

    private void sendMessage(String txt, Player player) {
        player.sendSystemMessage(Component.literal(txt));
    }

    private InteractionResult processClick(RecorderDataComponent.RecorderMode mode, Player player, Level level, BlockCapabilityCache<IItemHandler, @Nullable Direction> capCache, BlockPos pos) throws SQLException {
        var invRecord = getRecord(level, pos);
        //TODO: bug sometimes this method is executed two times, might be already resolved.
        switch (mode) {
            case REMOVE:
                if(invRecord.isEmpty()) {
                    this.sendMessage("This block in not monitored.", player);
                    return InteractionResult.PASS;
                }
                this.statManager.unmonitore(invRecord.get().id());
                this.sendMessage("Removed inventory from monitoring.", player);
                return InteractionResult.SUCCESS;
            case ADD:
                if(invRecord.isPresent()) {
                    this.sendMessage("Already monitored.", player);
                    return InteractionResult.PASS;
                }
                int playerId = database.getPlayerDao().insertIfNotExists(player.getName().getString());
                int invId = database.getInventoryDao().insert(
                        level.dimension().location().toString(),
                        pos.getX(), pos.getY(), pos.getZ(),
                        "ITEM");
                //associate inventory to player
                this.database.getPlayerInventoryDao().insert(playerId, invId);
                //start monitoring for the block
                this.statManager.add(invId, capCache);
                this.sendMessage("Added inventory to monitoring.", player);
                return InteractionResult.SUCCESS;
        }
        System.err.println("UNKNOW MODE '" + mode + "'");
        return InteractionResult.PASS;
    }

}
