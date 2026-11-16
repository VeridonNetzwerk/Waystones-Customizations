package dev.waystonescustomizations.gui.popup;

import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.custom.CustomImages;
import dev.waystonescustomizations.gui.UiDraw;
import dev.waystonescustomizations.icon.GroupIcons;
import dev.waystonescustomizations.style.EntryBackground;
import dev.waystonescustomizations.style.EntryStyles;
import dev.waystonescustomizations.theme.Theme;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneGroup;
import net.blay09.mods.waystones.client.WaystoneGroupIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Picks the background of one waystone entry, or of all entries of its group.
 * Options: group colour (auto), plain theme, 16 colours, block textures and the player's own pictures.
 */
public class BackgroundPopup extends Popup {
    private static final String INHERIT = "inherit";
    private static final String TEXT = "text:";
    private static final String TEXT_AUTO = "text:auto";
    private static final int CELL = 18;
    private static final int COLUMNS = 9;
    private static final int PAD = 7;
    private static final int LABEL = 12;
    private static final int ICON_BUTTON = 14;

    private record Cell(int x, int y, String value) {
    }

    private record Label(int y, Component text) {
    }

    private final @Nullable Waystone waystone;
    private final @Nullable WaystoneGroup group;
    private final int screenWidth;
    private final int screenHeight;
    private boolean groupTarget;
    private final List<Cell> cells = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private int tabsY;

    public BackgroundPopup(Waystone waystone, List<WaystoneGroup> groups, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        this(waystone, groups.isEmpty() ? null : groups.getFirst(), mouseX, mouseY, screenWidth, screenHeight);
    }

    /** Only for one group, opened from the group editor. */
    public static BackgroundPopup forGroup(WaystoneGroup group, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        return new BackgroundPopup(null, group, mouseX, mouseY, screenWidth, screenHeight);
    }

    private BackgroundPopup(@Nullable Waystone waystone, @Nullable WaystoneGroup group, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        this.waystone = waystone;
        this.group = group;
        this.groupTarget = waystone == null;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        width = COLUMNS * CELL + 2 * PAD + 6;
        x = mouseX + 8;
        y = mouseY - 30;
        build();
    }

    private void build() {
        cells.clear();
        labels.clear();
        int top = 0;
        List<String> basics = new ArrayList<>();
        if (!groupTarget && group != null) {
            basics.add(INHERIT);
        }
        basics.add(EntryBackground.AUTO);
        basics.add(EntryBackground.THEME);
        top = section(top, "waystonescustomizations.background.section.basic", basics);
        List<String> texts = new ArrayList<>();
        texts.add(TEXT_AUTO);
        EntryBackground.COLORS.forEach(c -> texts.add(TEXT + c));
        top = section(top, "waystonescustomizations.background.section.text", texts);
        top = section(top, "waystonescustomizations.background.section.colors", EntryBackground.COLORS);
        top = section(top, "waystonescustomizations.background.section.blocks",
                EntryBackground.BLOCKS.stream().map(b -> "block:" + b).toList());
        List<String> custom = CustomImages.get(CustomImages.Kind.BACKGROUNDS).stream().map(i -> "custom:" + i.key()).toList();
        top = section(top, "waystonescustomizations.background.section.custom", custom);
        if (custom.isEmpty()) {
            top += 2 * 10 + 4;
        }
        contentHeight = top;

        tabsY = y + 18;
        int headerHeight = 18 + (hasTabs() ? 20 : 0);
        int footer = 16;
        height = Math.min(headerHeight + PAD + contentHeight + footer + PAD, screenHeight - 8);
        clampToScreen(screenWidth, screenHeight);
        tabsY = y + 18;
        viewX = x + PAD;
        viewY = y + headerHeight + PAD - 2;
        viewW = COLUMNS * CELL + 1;
        viewH = height - (viewY - y) - footer - PAD;
        scroll = Math.clamp(scroll, 0, maxScroll());
    }

    private int section(int top, String title, List<String> values) {
        labels.add(new Label(top, Component.translatable(title)));
        top += LABEL;
        for (int i = 0; i < values.size(); i++) {
            cells.add(new Cell((i % COLUMNS) * CELL, top + (i / COLUMNS) * CELL, values.get(i)));
        }
        return top + ((values.size() + COLUMNS - 1) / COLUMNS) * CELL + 3;
    }

    private boolean hasTabs() {
        return group != null && waystone != null;
    }

    private String current() {
        EntryStyles styles = EntryStyles.get();
        if (groupTarget && group != null) {
            String value = styles.group(group.identifier());
            return value != null ? value : EntryBackground.AUTO;
        }
        if (waystone == null) {
            return EntryBackground.AUTO;
        }
        String own = styles.waystone(waystone.getWaystoneUid());
        if (own != null) {
            return own;
        }
        return group != null ? INHERIT : EntryBackground.AUTO;
    }

    private String currentText() {
        EntryStyles styles = EntryStyles.get();
        String value = groupTarget && group != null ? styles.groupText(group.identifier())
                : waystone != null ? styles.waystoneText(waystone.getWaystoneUid()) : null;
        return value != null ? TEXT + value : TEXT_AUTO;
    }

    private void chooseText(String value) {
        String color = value.equals(TEXT_AUTO) ? null : value.substring(TEXT.length());
        if (groupTarget && group != null) {
            EntryStyles.get().setGroupText(group.identifier(), color);
        } else if (waystone != null) {
            EntryStyles.get().setWaystoneText(waystone.getWaystoneUid(), color);
        }
    }

    private Component optionName(String value) {
        if (value.equals(INHERIT) && group != null) {
            return Component.translatable("waystonescustomizations.background.inherit", group.name());
        }
        if (value.equals(TEXT_AUTO)) {
            return Component.translatable("waystonescustomizations.text.auto");
        }
        if (value.startsWith(TEXT)) {
            return Component.translatable("waystonescustomizations.text.color", EntryBackground.name(value.substring(TEXT.length())));
        }
        return EntryBackground.name(value);
    }

    private void choose(String value) {
        if (value.startsWith(TEXT)) {
            chooseText(value);
            return;
        }
        EntryStyles styles = EntryStyles.get();
        if (groupTarget && group != null) {
            styles.setGroup(group.identifier(), value.equals(EntryBackground.AUTO) ? null : value);
        } else if (waystone == null) {
            return;
        } else if (value.equals(INHERIT) || (group == null && value.equals(EntryBackground.AUTO))) {
            styles.setWaystone(waystone.getWaystoneUid(), null);
        } else {
            styles.setWaystone(waystone.getWaystoneUid(), value);
        }
    }

    private static int luminance(int rgb) {
        return (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
    }

    private int groupColor() {
        return group != null ? group.color() & 0xFFFFFF : 0;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        drawWindow(g);
        String title = waystone != null ? waystone.getName().getString() : group != null ? group.name().getString() : "";
        String name = font.plainSubstrByWidth(title, width - 2 * PAD - 2 * ICON_BUTTON - 6);
        UiDraw.label(g, name.isEmpty() ? Component.translatable("waystonescustomizations.background.title").getString() : name,
                x + PAD, y + 7, false);
        boolean folderHover = UiDraw.iconButton(g, folderX(), y + 4, ICON_BUTTON, "folder", mouseX, mouseY);
        boolean reloadHover = UiDraw.iconButton(g, reloadX(), y + 4, ICON_BUTTON, "reload", mouseX, mouseY);

        if (hasTabs()) {
            int half = (width - 2 * PAD - 2) / 2;
            UiDraw.textButton(g, x + PAD, tabsY, half, 18, Component.translatable("waystonescustomizations.background.target.waystone"),
                    !groupTarget, mouseX, mouseY);
            UiDraw.textButton(g, x + PAD + half + 2, tabsY, half, 18,
                    Component.translatable("waystonescustomizations.background.target.group", group.name()), groupTarget, mouseX, mouseY);
        }

        UiDraw.inset(g, viewX - 1, viewY - 1, viewW + 2, viewH + 2);
        Theme theme = ThemeConfig.get().activeTheme();
        String current = current();
        String currentText = currentText();
        String hoveredValue = null;
        g.enableScissor(viewX, viewY, viewX + viewW, viewY + viewH);
        for (Label label : labels) {
            int ly = viewY + label.y() - scroll;
            g.text(font, label.text(), viewX + 2, ly + 2, 0xFFFFFFFF, true);
        }
        for (Cell cell : cells) {
            int cx = viewX + cell.x();
            int cy = viewY + cell.y() - scroll;
            if (cy + CELL < viewY || cy > viewY + viewH) {
                continue;
            }
            UiDraw.slot(g, cx, cy, CELL);
            if (cell.value().startsWith(TEXT)) {
                int rgb = cell.value().equals(TEXT_AUTO)
                        ? (group != null ? groupColor() : 0xFFFFFF)
                        : EntryBackground.color(cell.value().substring(TEXT.length())).orElse(0xFFFFFF);
                g.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, luminance(rgb) < 90 ? 0xFFC8C8C8 : 0xFF202020);
                String letter = cell.value().equals(TEXT_AUTO) ? "A" : "a";
                g.text(font, letter, cx + (CELL - font.width(letter)) / 2, cy + 5, 0xFF000000 | rgb, false);
            } else if (cell.value().equals(INHERIT) && group != null) {
                String groupValue = EntryStyles.get().group(group.identifier());
                EntryBackground.drawSwatch(g, groupValue != null ? groupValue : EntryBackground.AUTO, groupColor(), theme, cx + 1, cy + 1, 16);
                GroupIcons.draw(g, WaystoneGroupIcons.getIcon(group), cx + 3, cy + 3, 12, 12);
            } else {
                EntryBackground.drawSwatch(g, cell.value(), groupColor(), theme, cx + 1, cy + 1, 16);
                if (cell.value().equals(EntryBackground.AUTO)) {
                    g.text(font, "A", cx + 6, cy + 5, 0xFFFFFFFF, true);
                }
            }
            if (cell.value().equals(current) || cell.value().equals(currentText)) {
                g.outline(cx, cy, CELL, CELL, 0xFFFFFF80);
                g.outline(cx + 1, cy + 1, CELL - 2, CELL - 2, UiDraw.accent());
            }
            if (inView(mouseX, mouseY) && UiDraw.inside(mouseX, mouseY, cx, cy, CELL, CELL)) {
                g.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, 0x60FFFFFF);
                hoveredValue = cell.value();
            }
        }
        if (CustomImages.get(CustomImages.Kind.BACKGROUNDS).isEmpty()) {
            int hy = viewY + contentHeight - 24 - scroll;
            UiDraw.label(g, Component.translatable("waystonescustomizations.background.no_custom.1"), viewX + 2, hy, true);
            UiDraw.label(g, Component.translatable("waystonescustomizations.background.no_custom.2"), viewX + 2, hy + 10, true);
        }
        g.disableScissor();
        drawScrollbar(g);

        String shown = hoveredValue != null ? hoveredValue : current;
        Component footer = optionName(shown);
        UiDraw.label(g, font.plainSubstrByWidth(footer.getString(), width - 2 * PAD), x + PAD, y + height - PAD - 10,
                hoveredValue == null);

        if (folderHover) {
            g.setTooltipForNextFrame(font, Component.translatable("waystonescustomizations.button.open_backgrounds"), mouseX, mouseY);
        } else if (reloadHover) {
            g.setTooltipForNextFrame(font, Component.translatable("waystonescustomizations.button.reload"), mouseX, mouseY);
        }
    }

    private int folderX() {
        return x + width - PAD - 2 * ICON_BUTTON - 2;
    }

    private int reloadX() {
        return x + width - PAD - ICON_BUTTON;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (UiDraw.inside(mouseX, mouseY, folderX(), y + 4, ICON_BUTTON, ICON_BUTTON)) {
            click();
            CustomImages.openFolder(CustomImages.Kind.BACKGROUNDS);
            return;
        }
        if (UiDraw.inside(mouseX, mouseY, reloadX(), y + 4, ICON_BUTTON, ICON_BUTTON)) {
            click();
            CustomImages.reload();
            build();
            return;
        }
        if (hasTabs() && mouseY >= tabsY && mouseY < tabsY + 18) {
            int half = (width - 2 * PAD - 2) / 2;
            boolean toGroup = mouseX >= x + PAD + half + 2;
            if (toGroup != groupTarget) {
                click();
                groupTarget = toGroup;
                build();
            }
            return;
        }
        if (!inView(mouseX, mouseY)) {
            return;
        }
        for (Cell cell : cells) {
            int cx = viewX + cell.x();
            int cy = viewY + cell.y() - scroll;
            if (UiDraw.inside(mouseX, mouseY, cx, cy, CELL, CELL)) {
                click();
                choose(cell.value());
                return;
            }
        }
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
