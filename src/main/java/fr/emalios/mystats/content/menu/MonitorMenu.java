package fr.emalios.mystats.content.menu;

import fr.emalios.mystats.core.dao.InventoryDao;
import fr.emalios.mystats.core.db.Database;
import fr.emalios.mystats.core.stat.StatCalculator;
import fr.emalios.mystats.helper.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static fr.emalios.mystats.registries.ModMenus.MONITOR_MENU;

public class MonitorMenu extends AbstractContainerMenu {

    private final Player player;
    private final StatCalculator statCalculator = StatCalculator.getInstance();
    private Map<String, Double> stats = new HashMap<>();

    public MonitorMenu(int containerId, Inventory playerInv) {
        super(MONITOR_MENU.get(), containerId);
        this.player = playerInv.player;
        try {
            this.updateStats();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStats() throws SQLException {
        System.out.println("Updating stats");
        this.stats = this.statCalculator.genPerSecond(this.player.getName().getString());
        System.out.println(this.stats);
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