package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.IconPickerWidget;
import dev.waystonescustomizations.gui.ThemeButton;
import dev.waystonescustomizations.gui.ThemedScreen;
import net.blay09.mods.waystones.client.gui.screen.WaystoneContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Every Waystones window (waystone list, group editor, group management, rename, settings) gets the
 * theme panel behind it. The panel is the box around all Waystones widgets plus the title above them.
 */
@Mixin(WaystoneContainerScreen.class)
public abstract class WaystoneContainerScreenMixin<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements ThemedScreen {
    private static final int PANEL_PADDING = 7;
    /** Room above the first widget for the title / location header that Waystones draws. */
    private static final int HEADER_SPACE = 20;

    private WaystoneContainerScreenMixin(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public int[] waystonescustomizations$panelBounds() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (GuiEventListener child : children()) {
            if (child instanceof AbstractWidget widget && widget.visible
                    && !(widget instanceof ThemeButton) && !(widget instanceof IconPickerWidget)) {
                minX = Math.min(minX, widget.getX());
                minY = Math.min(minY, widget.getY());
                maxX = Math.max(maxX, widget.getRight());
                maxY = Math.max(maxY, widget.getBottom());
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return new int[]{leftPos - PANEL_PADDING, topPos - PANEL_PADDING, imageWidth + PANEL_PADDING * 2, imageHeight + PANEL_PADDING * 2};
        }
        int titleHalf = font.width(title) / 2;
        int center = width / 2;
        minX = Math.min(minX, center - titleHalf);
        maxX = Math.max(maxX, center + titleHalf);
        minY -= HEADER_SPACE;
        minX -= PANEL_PADDING;
        maxX += PANEL_PADDING;
        minY -= PANEL_PADDING;
        maxY += PANEL_PADDING;
        minY = Math.max(2, minY);
        return new int[]{minX, minY, maxX - minX, maxY - minY, minY + PANEL_PADDING + HEADER_SPACE};
    }
}
