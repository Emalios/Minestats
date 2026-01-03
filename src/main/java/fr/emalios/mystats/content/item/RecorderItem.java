package fr.emalios.mystats.content.item;

import fr.emalios.mystats.api.Inventory;
import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.impl.storage.db.Database;
import fr.emalios.mystats.api.stat.IHandler;
import fr.emalios.mystats.impl.adapter.StatManager;
import fr.emalios.mystats.helper.Utils;
import fr.emalios.mystats.registries.StatDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;

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
            this.sendMessage("Set to '" + mode + "' mode.", player);
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
                this.sendMessage("Set to '" + mode + "' mode.", player);
                itemStack.set(StatDataComponent.RECORDER_COMPONENT, new RecorderDataComponent.RecorderData(mode));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        //Test if block has at least one Capability
        List<IHandler> handlers = Utils.getIHandlers(level, pos);
        if(handlers.isEmpty()) {
            return InteractionResult.PASS;
        }
        try {
            return this.processClick(mode, player, level, handlers, pos);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(String txt, Player player) {
        player.displayClientMessage(Component.literal(txt), true);
    }

    private InteractionResult processClick(RecorderDataComponent.RecorderMode mode, Player player, Level level, List<IHandler> handlers, BlockPos pos) throws SQLException {
        String playerName = player.getName().getString();
        //TODO: bug sometimes this method is executed two times, might be already resolved.
        switch (mode) {
            case REMOVE:
                Optional<Inventory> optInv = Storage.inventories().findByPos(
                        level.dimension().location().toString(),
                        pos.getX(), pos.getY(), pos.getZ());
                if(optInv.isEmpty()) {
                    this.sendMessage("This block in not monitored.", player);
                    return InteractionResult.PASS;
                }
                this.statManager.unmonitore(optInv.get());
                this.sendMessage("Removed inventory from monitoring.", player);
                return InteractionResult.SUCCESS;
            case ADD:
                StatPlayer statPlayer = Storage.players().getOrCreate(playerName);
                Inventory inventory = Storage.inventories().getOrCreate(
                        level.dimension().location().toString(),
                        pos.getX(), pos.getY(), pos.getZ());
                inventory.addHandlers(handlers);
                if(statPlayer.hasInventory(inventory)) {
                    this.sendMessage("Already monitored.", player);
                    return InteractionResult.PASS;
                }
                statPlayer.addInventory(inventory);
                //start monitoring for the block
                this.statManager.add(inventory);
                this.sendMessage("Added inventory to monitoring.", player);
                return InteractionResult.SUCCESS;
        }
        System.err.println("UNKNOW MODE '" + mode + "'");
        return InteractionResult.PASS;
    }
}
