package dev.waystonescustomizations.gui;

import dev.waystonescustomizations.custom.CustomImages;
import dev.waystonescustomizations.icon.GroupIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Icon picker next to the Waystones group editor: tabs (places, nature, resources, danger, worlds,
 * Waystones, items, own pictures), a scrollable grid and the name of the icon under the mouse.
 */
public class IconPickerWidget extends AbstractWidget implements ScrollTarget {
    public static final int CELL = 18;
    public static final int PADDING = 5;
    public static final int HEADER = 14;
    public static final int NAME_LINE = 12;
    private static final int ICON_BUTTON = 12;
    private static final GroupIcons.Tab[] TABS = GroupIcons.Tab.values();

    private final int columns;
    private final int visibleRows;
    private final Supplier<Identifier> selected;
    private final Consumer<Identifier> onPick;
    private GroupIcons.Tab tab;
    private List<Identifier> icons;
    private int scrollRow;

    public IconPickerWidget(int x, int y, int columns, int visibleRows, Supplier<Identifier> selected, Consumer<Identifier> onPick) {
        super(x, y, widthFor(columns), heightFor(columns, visibleRows), Component.translatable("waystonescustomizations.picker.title"));
        this.columns = columns;
        this.visibleRows = visibleRows;
        this.selected = selected;
        this.onPick = onPick;
        this.tab = GroupIcons.tabOf(selected.get());
        this.icons = GroupIcons.icons(tab);
        scrollToSelected();
    }

    public static int widthFor(int columns) {
        return columns * CELL + PADDING * 2 + 4;
    }

    private static int tabRows(int columns) {
        return (TABS.length + columns - 1) / columns;
    }

    public static int heightFor(int columns, int rows) {
        return HEADER + tabRows(columns) * CELL + 4 + rows * CELL + NAME_LINE + PADDING * 2;
    }

    private void selectTab(GroupIcons.Tab newTab) {
        tab = newTab;
        icons = GroupIcons.icons(tab);
        scrollRow = 0;
        scrollToSelected();
    }

    private int totalRows() {
        return (icons.size() + columns - 1) / columns;
    }

    private int maxScroll() {
        return Math.max(0, totalRows() - visibleRows);
    }

    private void scrollToSelected() {
        int index = icons.indexOf(selected.get());
        if (index >= 0) {
            scrollRow = Math.clamp(index / columns - visibleRows / 2, 0, maxScroll());
        }
    }

    private int tabsY() {
        return getY() + HEADER + PADDING - 2;
    }

    private int gridX() {
        return getX() + PADDING;
    }

    private int gridY() {
        return tabsY() + tabRows(columns) * CELL + 4;
    }

    private int tabAt(double mouseX, double mouseY) {
        int col = (int) Math.floor((mouseX - gridX()) / CELL);
        int row = (int) Math.floor((mouseY - tabsY()) / CELL);
        if (col < 0 || col >= columns || row < 0 || row >= tabRows(columns)) {
            return -1;
        }
        int index = row * columns + col;
        return index < TABS.length ? index : -1;
    }

    private int indexAt(double mouseX, double mouseY) {
        int col = (int) Math.floor((mouseX - gridX()) / CELL);
        int row = (int) Math.floor((mouseY - gridY()) / CELL);
        if (col < 0 || col >= columns || row < 0 || row >= visibleRows) {
            return -1;
        }
        int index = (row + scrollRow) * columns + col;
        return index < icons.size() ? index : -1;
    }

    private int folderX() {
        return getX() + getWidth() - PADDING - 2 * ICON_BUTTON - 4;
    }

    private int reloadX() {
        return getX() + getWidth() - PADDING - ICON_BUTTON - 2;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        Font font = Minecraft.getInstance().font;
        int x = getX();
        int y = getY();
        UiDraw.window(g, x, y, getWidth(), getHeight());
        UiDraw.label(g, tab.title(), x + PADDING + 1, y + 6, false);

        boolean folderHover = false;
        boolean reloadHover = false;
        if (tab == GroupIcons.Tab.CUSTOM) {
            folderHover = UiDraw.iconButton(g, folderX(), y + 3, ICON_BUTTON, "folder", mouseX, mouseY);
            reloadHover = UiDraw.iconButton(g, reloadX(), y + 3, ICON_BUTTON, "reload", mouseX, mouseY);
        }

        // tabs
        int hoveredTab = isMouseOver(mouseX, mouseY) ? tabAt(mouseX, mouseY) : -1;
        for (int i = 0; i < TABS.length; i++) {
            int tx = gridX() + (i % columns) * CELL;
            int ty = tabsY() + (i / columns) * CELL;
            if (TABS[i] == tab) {
                UiDraw.inset(g, tx, ty, CELL, CELL);
                g.outline(tx, ty, CELL, CELL, UiDraw.accent());
            } else {
                UiDraw.button(g, tx, ty, CELL, CELL, i == hoveredTab, true);
            }
            GroupIcons.draw(g, TABS[i].tabIcon(), tx + 1, ty + 1, 16, 16);
        }
        g.fill(gridX(), gridY() - 3, gridX() + columns * CELL, gridY() - 2, UiDraw.classic() ? 0xFF8B8B8B : 0x60FFFFFF);

        // grid
        Identifier current = selected.get();
        int hovered = isMouseOver(mouseX, mouseY) ? indexAt(mouseX, mouseY) : -1;
        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = (row + scrollRow) * columns + col;
                int cx = gridX() + col * CELL;
                int cy = gridY() + row * CELL;
                UiDraw.slot(g, cx, cy, CELL);
                if (index >= icons.size()) {
                    continue;
                }
                Identifier icon = icons.get(index);
                if (icon.equals(current)) {
                    g.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, (UiDraw.accent() & 0xFFFFFF) | 0xA0000000);
                }
                GroupIcons.draw(g, icon, cx + 1, cy + 1, 16, 16);
                if (index == hovered) {
                    g.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, 0x80FFFFFF);
                }
            }
        }
        if (icons.isEmpty() && tab == GroupIcons.Tab.CUSTOM) {
            int maxWidth = columns * CELL - 4;
            List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.translatable("waystonescustomizations.picker.no_custom"), maxWidth);
            for (int i = 0; i < lines.size() && i < visibleRows * 2; i++) {
                g.text(font, lines.get(i), gridX() + 3, gridY() + 3 + i * 9, UiDraw.classic() ? 0xFF303030 : 0xFFE0E0E0, !UiDraw.classic());
            }
        }

        // scrollbar
        if (maxScroll() > 0) {
            int trackX = gridX() + columns * CELL + 1;
            int trackY = gridY();
            int trackH = visibleRows * CELL;
            g.fill(trackX, trackY, trackX + 3, trackY + trackH, 0xFF373737);
            int thumbH = Math.max(8, trackH * visibleRows / totalRows());
            int thumbY = trackY + (trackH - thumbH) * scrollRow / maxScroll();
            g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFFFFFFF);
        }

        // name of the hovered (or chosen) icon
        Identifier named = hovered >= 0 ? icons.get(hovered) : current;
        if (named != null) {
            String name = font.plainSubstrByWidth(GroupIcons.displayName(named).getString(), columns * CELL);
            UiDraw.label(g, name, gridX() + 1, gridY() + visibleRows * CELL + 3, hovered < 0);
        }

        if (hoveredTab >= 0) {
            g.setTooltipForNextFrame(font, TABS[hoveredTab].title(), mouseX, mouseY);
        } else if (folderHover) {
            g.setTooltipForNextFrame(font, Component.translatable("waystonescustomizations.button.open_icons"), mouseX, mouseY);
        } else if (reloadHover) {
            g.setTooltipForNextFrame(font, Component.translatable("waystonescustomizations.button.reload"), mouseX, mouseY);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (tab == GroupIcons.Tab.CUSTOM) {
            if (UiDraw.inside(mx, my, folderX(), getY() + 3, ICON_BUTTON, ICON_BUTTON)) {
                CustomImages.openFolder(CustomImages.Kind.ICONS);
                return;
            }
            if (UiDraw.inside(mx, my, reloadX(), getY() + 3, ICON_BUTTON, ICON_BUTTON)) {
                CustomImages.reload();
                selectTab(tab);
                return;
            }
        }
        int tabIndex = tabAt(mx, my);
        if (tabIndex >= 0) {
            selectTab(TABS[tabIndex]);
            return;
        }
        int index = indexAt(mx, my);
        if (index >= 0) {
            onPick.accept(icons.get(index));
        }
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean waystonescustomizations$scroll(double mouseX, double mouseY, double scrollY) {
        if (!visible || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (maxScroll() > 0) {
            scrollRow = Math.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScroll());
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return waystonescustomizations$scroll(mouseX, mouseY, scrollY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
