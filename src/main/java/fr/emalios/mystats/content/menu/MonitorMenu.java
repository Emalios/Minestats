package fr.emalios.mystats.content.menu;

import fr.emalios.mystats.api.StatPlayer;
import fr.emalios.mystats.api.stat.Stat;
import fr.emalios.mystats.api.stat.utils.StatCalculator;
import fr.emalios.mystats.api.storage.Storage;
import fr.emalios.mystats.network.StatPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static fr.emalios.mystats.registries.ModMenus.MONITOR_MENU;

public class MonitorMenu extends AbstractContainerMenu {

    private int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 300;

    private final Player player;
    private final StatPlayer statPlayer;
    private final StatCalculator statCalculator = StatCalculator.getInstance();
    private List<Stat> stats = new ArrayList<>();

    public MonitorMenu(int containerId, Inventory playerInv) {
        super(MONITOR_MENU.get(), containerId);
        this.player = playerInv.player;
        this.statPlayer = Storage.players().getOrCreate(player.getName().getString());
        try {
            this.updateStats();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL) {
            tickCounter = 0;
            try {
                //TODO: send only if stats has changed
                this.updateStats();
                PacketDistributor.sendToPlayer((ServerPlayer) this.player, new StatPayload(this.stats));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void updateStats() throws SQLException {
        this.stats = this.statCalculator.genPerSecond(this.statPlayer.getInventories()).stream().toList();
    }

    @Override
    public boolean stillValid(Player p) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    public List<Stat> getStats() {
        return this.stats;
    }
}