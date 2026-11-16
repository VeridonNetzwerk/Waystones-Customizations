package dev.waystonescustomizations.gui;

import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.icon.GroupIcons;
import dev.waystonescustomizations.theme.Theme;
import dev.waystonescustomizations.theme.ThemeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Window, slots, buttons and text for all our own GUI parts. Everything follows the active theme:
 * with the Waystones default or the inventory theme it is the classic light grey Minecraft look,
 * with every other theme it is the theme's panel with dark slots and white text.
 */
public final class UiDraw {
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_HOVER = Identifier.withDefaultNamespace("widget/button_highlighted");
    private static final Identifier BUTTON_OFF = Identifier.withDefaultNamespace("widget/button_disabled");

    private UiDraw() {
    }

    public static Theme theme() {
        return ThemeConfig.get().activeTheme();
    }

    /** True for the classic light grey look (Waystones default and inventory theme). */
    public static boolean classic() {
        Theme theme = theme();
        return theme.isVanilla() || theme.background() == null;
    }

    public static void window(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (classic()) {
            g.fill(x + 1, y, x + w - 1, y + h, 0xFF000000);
            g.fill(x, y + 1, x + w, y + h - 1, 0xFF000000);
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF555555);
            g.fill(x + 1, y + 1, x + w - 3, y + h - 3, 0xFFFFFFFF);
            g.fill(x + 3, y + 3, x + w - 3, y + h - 3, 0xFFC6C6C6);
        } else {
            ThemeRenderer.drawPanel(g, x, y, w, h, theme());
        }
    }

    /** Sunken inventory slot. */
    public static void slot(GuiGraphicsExtractor g, int x, int y, int size) {
        inset(g, x, y, size, size);
    }

    /** Sunken area for slots and lists. */
    public static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        if (classic()) {
            g.fill(x, y, x + w, y + h, 0xFF8B8B8B);
            g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
            g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
            g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
            g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
        } else {
            g.fill(x, y, x + w, y + h, 0x88000000);
            g.fill(x, y, x + w - 1, y + 1, 0xC0000000);
            g.fill(x, y, x + 1, y + h - 1, 0xC0000000);
            g.fill(x + 1, y + h - 1, x + w, y + h, 0x40FFFFFF);
            g.fill(x + w - 1, y + 1, x + w, y + h, 0x40FFFFFF);
        }
    }

    /** Background of a category heading in a list. */
    public static int headingFill() {
        return classic() ? 0xFF6E6E6E : (theme().frameDark() & 0xFFFFFF) | 0xD0000000;
    }

    /** Highlight colour of the chosen element. */
    public static int accent() {
        return classic() ? 0xFF5AA02C : theme().accent();
    }

    /** Text on the window itself. {@code dim} for hints and secondary text. */
    public static void label(GuiGraphicsExtractor g, Component text, int x, int y, boolean dim) {
        label(g, text.getString(), x, y, dim);
    }

    public static void label(GuiGraphicsExtractor g, String text, int x, int y, boolean dim) {
        Font font = Minecraft.getInstance().font;
        if (classic()) {
            g.text(font, text, x, y, dim ? 0xFF707070 : 0xFF404040, false);
        } else {
            g.text(font, text, x, y, dim ? 0xFFB8B8B8 : 0xFFFFFFFF, true);
        }
    }

    public static void button(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered, boolean enabled) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, !enabled ? BUTTON_OFF : hovered ? BUTTON_HOVER : BUTTON, x, y, w, h);
    }

    /** Button with centred text; returns true if the mouse is over it. */
    public static boolean textButton(GuiGraphicsExtractor g, int x, int y, int w, int h, Component text, boolean selected, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        button(g, x, y, w, h, hovered, !selected);
        Font font = Minecraft.getInstance().font;
        String label = font.plainSubstrByWidth(text.getString(), w - 6);
        g.text(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2, selected ? 0xFFFFFF80 : 0xFFFFFFFF, true);
        return hovered;
    }

    /** Small square button with one of our ui icons; returns true if hovered. */
    public static boolean iconButton(GuiGraphicsExtractor g, int x, int y, int size, String icon, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        button(g, x, y, size, size, hovered, true);
        int inner = size - 4;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, GroupIcons.sprite(icon), x + 2, y + 2, inner, inner);
        return hovered;
    }

    public static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
