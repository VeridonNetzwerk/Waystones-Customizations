package dev.waystonescustomizations.gui.popup;

import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.custom.CustomImages;
import dev.waystonescustomizations.gui.UiDraw;
import dev.waystonescustomizations.style.EntryBackground;
import dev.waystonescustomizations.theme.Theme;
import dev.waystonescustomizations.theme.Themes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * List of all themes, sorted by category. Moving the mouse over a theme shows it right away
 * behind the list, clicking picks it.
 */
public class ThemeListPopup extends Popup {
    private static final int HEADER_ROW = 13;
    private static final int THEME_ROW = 18;
    private static final int PAD = 7;
    private static final int TITLE = 16;
    private static final int ICON_BUTTON = 14;

    /** One line of the list: a category heading (theme == null) or a theme. */
    private record Row(int top, int height, @Nullable String category, @Nullable Theme theme) {
    }

    private final List<Row> rows = new ArrayList<>();
    private final int screenWidth;
    private final int screenHeight;
    private final int anchorX;
    private final int anchorY;
    private final int anchorRight;

    /**
     * @param anchorX     left edge of the button that opened the list
     * @param anchorRight right edge of that button
     */
    public ThemeListPopup(int anchorX, int anchorRight, int anchorY, int screenWidth, int screenHeight) {
        this.anchorX = anchorX;
        this.anchorRight = anchorRight;
        this.anchorY = anchorY;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        build();
    }

    private void build() {
        rows.clear();
        List<Theme> themes = Themes.all();
        int top = 0;
        int nameWidth = font.width(Component.translatable("waystonescustomizations.themes.title")) + 2 * ICON_BUTTON;
        for (String category : Themes.CATEGORIES) {
            List<Theme> inCategory = themes.stream().filter(t -> t.category().equals(category)).toList();
            if (inCategory.isEmpty() && !category.equals("custom")) {
                continue;
            }
            rows.add(new Row(top, HEADER_ROW, category, null));
            top += HEADER_ROW;
            for (Theme theme : inCategory) {
                rows.add(new Row(top, THEME_ROW, null, theme));
                top += THEME_ROW;
                nameWidth = Math.max(nameWidth, font.width(theme.displayName()));
            }
            if (category.equals("custom") && inCategory.isEmpty()) {
                rows.add(new Row(top, THEME_ROW, null, null));
                top += THEME_ROW;
            }
        }
        contentHeight = top;

        width = Math.clamp(nameWidth + 16 + 3 * PAD + 8, 130, 190);
        height = Math.min(TITLE + PAD + contentHeight + PAD + 2, screenHeight - 8);
        // right of the button if there is room, else left of it
        x = anchorRight + 4 + width <= screenWidth - 2 ? anchorRight + 4 : anchorX - 4 - width;
        y = anchorY;
        clampToScreen(screenWidth, screenHeight);
        viewX = x + PAD;
        viewY = y + TITLE + PAD - 2;
        viewW = width - 2 * PAD - 4;
        viewH = height - (viewY - y) - PAD;
        scroll = Math.clamp(scroll, 0, maxScroll());
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        drawWindow(g);
        UiDraw.label(g, Component.translatable("waystonescustomizations.themes.title"), x + PAD, y + 7, false);
        boolean folderHover = UiDraw.iconButton(g, folderX(), y + 4, ICON_BUTTON, "folder", mouseX, mouseY);
        boolean reloadHover = UiDraw.iconButton(g, reloadX(), y + 4, ICON_BUTTON, "reload", mouseX, mouseY);
        UiDraw.inset(g, viewX - 1, viewY - 1, viewW + 2, viewH + 2);

        ThemeConfig config = ThemeConfig.get();
        String selected = config.savedTheme().id();
        Theme hoveredTheme = null;
        g.enableScissor(viewX, viewY, viewX + viewW, viewY + viewH);
        for (Row row : rows) {
            int top = viewY + row.top() - scroll;
            if (top + row.height() < viewY || top > viewY + viewH) {
                continue;
            }
            if (row.category() != null) {
                g.fill(viewX, top, viewX + viewW, top + row.height(), UiDraw.headingFill());
                g.text(font, Component.translatable("waystonescustomizations.category." + row.category()), viewX + 3, top + 3, 0xFFFFFFFF, true);
                continue;
            }
            if (row.theme() == null) {
                UiDraw.label(g, Component.translatable("waystonescustomizations.themes.no_custom"), viewX + 4, top + 5, true);
                continue;
            }
            Theme theme = row.theme();
            boolean hovered = inView(mouseX, mouseY) && mouseY >= top && mouseY < top + row.height();
            boolean isSelected = theme.id().equals(selected);
            if (isSelected) {
                g.fill(viewX, top, viewX + viewW, top + row.height(), (UiDraw.accent() & 0xFFFFFF) | 0x90000000);
            }
            if (hovered) {
                g.fill(viewX, top, viewX + viewW, top + row.height(), 0x60FFFFFF);
                hoveredTheme = theme;
            }
            UiDraw.slot(g, viewX + 1, top + 1, 16);
            EntryBackground.drawSwatch(g, EntryBackground.THEME, 0, theme, viewX + 2, top + 2, 14);
            int color = isSelected ? 0xFFFFFFA0 : 0xFFFFFFFF;
            g.text(font, theme.displayName(), viewX + 22, top + 5, color, true);
        }
        g.disableScissor();
        drawScrollbar(g);
        config.setPreview(hoveredTheme);

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
        if (!inView(mouseX, mouseY)) {
            return;
        }
        double contentY = mouseY - viewY + scroll;
        for (Row row : rows) {
            if (row.theme() != null && contentY >= row.top() && contentY < row.top() + row.height()) {
                click();
                ThemeConfig config = ThemeConfig.get();
                config.theme = row.theme().id();
                config.save();
                Popups.close();
                return;
            }
        }
    }

    private static void click() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void onClose() {
        ThemeConfig.get().setPreview(null);
    }
}
