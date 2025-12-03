package fr.emalios.mystats.content.screen;

import fr.emalios.mystats.content.menu.MonitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class StatScreen extends AbstractContainerScreen<MonitorMenu> {


    private int scrollOffset = 0;
    private int maxScroll = 0;
    private final int cellHeight = 40;
    private final int cellWidth = 50;
    private final int panelPadding = 10;
    private final int scrollbarWidth = 6;
    private final int maxColumns = 4;

    private int panelWidth = 0;
    private int columnWidth = 0;
    private int columnStartX = 0;
    private int startX = 0;

    private Map<String, Double> stats;

    // Exemple de données : item → production par seconde

    public StatScreen(MonitorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        init();
    }

    public void updateStats(Map<String, Double> stats) {
        this.stats = stats;
    }

    @Override
    protected void init() {
        super.init();
        this.stats = this.menu.getStats();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //guiGraphics.fill(0, 0, this.width, this.height, 0xFF1E1E1E);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF1E1E1E);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // scrollY > 0 → molette vers le haut, <0 vers le bas
        scrollOffset -= scrollY * 20; // ajuster la vitesse selon besoin

        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        return true; // on consomme l'événement
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        //TODO: might be a problem when there is no items to render.
        //super.render(gfx, mouseX, mouseY, partialTicks);

        int totalItems = stats.size();
        int titleArea = 30; // espace réservé en haut pour le titre
        int bottomMargin = 10;
        final int verticalWindowMargin = 40;


        // 1) Calcul du nombre max de lignes visibles par colonne en fonction de l'espace UI autorisé.
        int maxAllowedHeight = Math.max(1, this.height - titleArea - bottomMargin - verticalWindowMargin);
        int itemsPerColumn = Math.max(1, maxAllowedHeight / cellHeight);

        // 2) Combien de colonnes il faudrait si on utilisait itemsPerColumn
        int neededColumns = (int) Math.ceil(totalItems / (double) itemsPerColumn);
        int totalColumns = Math.min(maxColumns, neededColumns);

        // 3) Combien de lignes réelles par colonne
        int rowsPerColumn = (int) Math.ceil(totalItems / (double) totalColumns);

        // 4) Hauteurs de contenu & visible (en px) et calcul du maxScroll
        int contentHeight = rowsPerColumn * cellHeight;
        int visibleHeight = Math.min(itemsPerColumn, rowsPerColumn) * cellHeight;
        maxScroll = Math.max(0, contentHeight - visibleHeight);

        // 5) Taillage panneau : largeur initiale (sans scrollbar) + ajout scrollbar si nécessaire
        // Si pas d'item on a une largeur minimale
        int initialPanelWidth = (totalColumns > 0 ? totalColumns : 2) * cellWidth + panelPadding * 2; // largeur dédiée aux colonnes + paddings
        int panelWidth = initialPanelWidth + (maxScroll > 0 ? scrollbarWidth : 0);
        int panelHeight = titleArea + visibleHeight; // hauteur globale du panneau

        // 6) Centrage du panneau globalement
        int startX = (this.width - panelWidth) / 2;
        int startY = (this.height - panelHeight) / 2;

        gfx.fill(startX, startY, startX + initialPanelWidth, startY + panelHeight, 0xAA222222);

        gfx.drawCenteredString(this.font, this.title, this.width / 2, startY + 10, 0xFFFFFF);

        // 7) Zone scrollable top/bottom
        int scrollAreaTop = startY + titleArea;
        int scrollAreaBottom = scrollAreaTop + visibleHeight;

        //TODO: bug sur la hauteur (items affichés trop bas en gui scale 3)

        // ---- centrer les colonnes à l'intérieur de la zone disponible (sans la scrollbar) ----
        int availableWidth = initialPanelWidth - panelPadding * 2; // largeur pour les colonnes si pas scrollbar
        //System.out.println("availableWidth: " + availableWidth);
        int columnsWidth = totalColumns * cellWidth;
        int columnsStartX = startX + panelPadding + (availableWidth - columnsWidth) / 2;
        this.columnWidth = columnsWidth;
        this.columnStartX = columnsStartX;

        // ---- clipping : scissor doit couvrir exactement la zone d'affichage des items ----
        int scissorLeft = columnsStartX - panelPadding; // padding left TODO: calculate left character like -999999
        int scissorTop = scrollAreaTop;
        int scissorRight = startX + initialPanelWidth - (maxScroll > 0 ? scrollbarWidth : 0);
        int scissorBottom = scrollAreaBottom;
        gfx.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);

        // ---- dessiner items (remplissage vertical) ----
        int i = 0;
        for (Map.Entry<String, Double> entry : this.stats.entrySet()) {
            int col = i / rowsPerColumn;
            int row = i % rowsPerColumn;

            int x = columnsStartX + col * cellWidth;
            int y = scrollAreaTop + row * cellHeight - scrollOffset;

            String itemId = entry.getKey();
            ResourceLocation rl = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(rl);
            // récupérer la registry des items

            // créer ItemStack
            ItemStack itemStack = new ItemStack(item);

            // ==== value & color ====
            double value = entry.getValue();
            int color = value >= 0 ? 0x00FF00 : 0xFF0000;

            // ==== rendre item ====
            if (!itemStack.isEmpty()) {
                gfx.renderItem(itemStack, x + (cellWidth - 16) / 2, y);
                gfx.renderItemDecorations(this.font, itemStack, x, y);
            }

            // ==== texte centré ====
            String txt = value + "/s";
            int textX = x + (cellWidth - this.font.width(txt)) / 2;
            gfx.drawString(this.font, txt, textX, y + 20, color);

            i++;
        }
        gfx.disableScissor();

        // ---- scrollbar (si nécessaire) ----
        if (maxScroll > 0) {
            int trackTop = scrollAreaTop;
            int trackBottom = scrollAreaBottom;
            int trackHeight = trackBottom - trackTop;

            int barHeight = (int) ((float) trackHeight * ((float) visibleHeight / (float) contentHeight));
            barHeight = Math.max(barHeight, 5);

            int barY = trackTop + (int) ((float) scrollOffset / maxScroll * (trackHeight - barHeight));
            int barX = startX + initialPanelWidth - scrollbarWidth;

            gfx.fill(barX, trackTop, barX + scrollbarWidth, trackBottom, 0x88000000); // rail
            gfx.fill(barX, barY, barX + scrollbarWidth, barY + barHeight, 0xFFFFFFFF); // thumb
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}