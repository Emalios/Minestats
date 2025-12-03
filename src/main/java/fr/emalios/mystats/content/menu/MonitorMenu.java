package fr.emalios.mystats.content.menu;

import fr.emalios.mystats.core.stat.utils.StatCalculator;
import fr.emalios.mystats.core.stat.RecordType;
import fr.emalios.mystats.network.StatPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import oshi.util.tuples.Pair;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static fr.emalios.mystats.registries.ModMenus.MONITOR_MENU;

public class MonitorMenu extends AbstractContainerMenu {

    private int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 300;

    private final Player player;
    private final StatCalculator statCalculator = StatCalculator.getInstance();
    private Map<String, Pair<Double, RecordType>> stats = new HashMap<>();

    public MonitorMenu(int containerId, Inventory playerInv) {
        super(MONITOR_MENU.get(), containerId);
        this.player = playerInv.player;
        try {
            this.updateStats();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // Tick toutes les X ticks
        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL) {
            tickCounter = 0;
            try {
                this.updateStats(); // serveur MAJ SQL
                PacketDistributor.sendToPlayer((ServerPlayer) this.player, new StatPayload(this.stats));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void updateStats() throws SQLException {
        this.stats = this.statCalculator.genPerSecond(this.player.getName().getString());
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    @Override
    public boolean stillValid(Player p) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

}