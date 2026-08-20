package fr.emalios.minestats.content.item;

import fr.emalios.minestats.MineStats;
import fr.emalios.minestats.api.StatsAPI;
import fr.emalios.minestats.api.models.inventory.Inventory;
import fr.emalios.minestats.api.models.inventory.Position;
import fr.emalios.minestats.api.models.StatPlayer;
import fr.emalios.minestats.helper.ConnectedBlocksFinder;
import fr.emalios.minestats.api.models.inventory.IHandler;
import fr.emalios.minestats.helper.Utils;
import fr.emalios.minestats.impl.McStatsAPI;
import fr.emalios.minestats.registries.StatDataComponent;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main item of the mod used to add and remove monitored blocks. Also used to show the stats for a clicked block
 * The behavior of this item is determined by his mode (RecorderMode) either ADD, REMOVE or VIEW
 */
public class RecorderItem extends Item {

    private final StatsAPI statsAPI = McStatsAPI.getInstance();

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
        return this.processClick(mode, player, level, pos);
    }

    private void sendMessage(String txt, Player player) {
        player.displayClientMessage(Component.literal(txt), true);
    }

    private InteractionResult processClick(RecorderDataComponent.RecorderMode mode, Player player, Level level, BlockPos pos) {
        String playerName = player.getName().getString();
        String world = level.dimension().location().toString();
        StatPlayer statPlayer = statsAPI.getPlayerService().getOrCreateByName(playerName);
        //use refence handlers to assert that all block has the same
        Set<BlockPos> connected = ConnectedBlocksFinder.getConnectedBlocks(level, pos, Utils.getIHandlers(level, pos));
        Set<Position> positions = connected.stream().map(blockPos -> new Position(
                world, blockPos.getX(), blockPos.getY(), blockPos.getZ()
        )).collect(Collectors.toSet());

        Optional<Inventory> optInv = statsAPI.getInventoryService().findByPos(new Position(
                world, pos.getX(), pos.getY(), pos.getZ()
        ));
        switch (mode) {
            case REMOVE:
                if(optInv.isEmpty()) {
                    this.sendMessage("This block is not monitored.", player);
                    return InteractionResult.PASS;
                }
                if(!statPlayer.hasInventory(optInv.get())) {
                    this.sendMessage("You did not monitored this block.", player);
                    return InteractionResult.PASS;
                }
                this.statsAPI.getPlayerService().removeInventoryFromPlayer(statPlayer, optInv.get());
                this.sendMessage("Removed inventory from monitoring.", player);
                return InteractionResult.SUCCESS;
            case ADD:
                if(optInv.isPresent()) {
                    MineStats.LOGGER.info("Inventory already exists");
                    if(statPlayer.hasInventory(optInv.get())) {
                        this.sendMessage("Already monitored.", player);
                        return InteractionResult.PASS;
                    }
                    this.addInventoryToPlayer(optInv.get(), statPlayer, player);
                    return InteractionResult.SUCCESS;
                }
                Inventory inventory = new Inventory(positions);
                MineStats.LOGGER.info("Adding inventory " + inventory + " to database.");
                this.statsAPI.getInventoryService().create(inventory);
                this.addInventoryToPlayer(inventory, statPlayer, player);
                return InteractionResult.SUCCESS;
            case DEBUG:
                if(optInv.isPresent()) {
                    if(statPlayer.hasInventory(optInv.get())) {
                        MineStats.LOGGER.info("[Minestats] Already monitored.");
                    } else {
                        MineStats.LOGGER.info("[Minestats] Not monitored.");
                    }
                }
                Inventory inv = new Inventory(positions);
                this.statsAPI.getInventoryService().addHandlersToInventory(inv);
                MineStats.LOGGER.info("[Minestats] Inventory: " + inv);
                return InteractionResult.SUCCESS;
        }
        System.err.println("UNKNOW MODE '" + mode + "'");
        return InteractionResult.PASS;
    }

    private void addInventoryToPlayer(Inventory inventory, StatPlayer statPlayer, Player player) {
        this.statsAPI.getPlayerService().addInventoryToPlayer(statPlayer, inventory);
        this.sendMessage("Added inventory to monitoring.", player);

    }
}
