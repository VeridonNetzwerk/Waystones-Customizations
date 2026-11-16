package dev.waystonescustomizations.theme;

import dev.waystonescustomizations.ThemeConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Draws the themed panel, list well and list entries. Everything is drawn in the Minecraft GUI style (bevelled edges). */
public final class ThemeRenderer {
    private ThemeRenderer() {
    }

    /** Big framed panel behind the whole waystone menu. */
    public static void drawPanel(GuiGraphicsExtractor g, int x, int y, int w, int h, Theme theme) {
        // black outline with cut corners, like vanilla windows
        g.fill(x + 1, y, x + w - 1, y + h, 0xFF000000);
        g.fill(x, y + 1, x + w, y + h - 1, 0xFF000000);
        // bevel
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, theme.frameDark());
        g.fill(x + 1, y + 1, x + w - 3, y + h - 3, theme.frameLight());

        int ix = x + 3;
        int iy = y + 3;
        int iw = w - 6;
        int ih = h - 6;
        if (theme.background() != null) {
            tileTexture(g, theme.background(), theme.texWidth(), theme.texHeight(), 64, ix, iy, iw, ih);
            int darkness = ThemeConfig.get().backgroundDarkness;
            if (darkness > 0) {
                g.fill(ix, iy, ix + iw, iy + ih, darkness << 24);
            }
            // soft vignette on the top and bottom
            g.fillGradient(ix, iy, ix + iw, iy + 10, 0x50000000, 0x00000000);
            g.fillGradient(ix, iy + ih - 10, ix + iw, iy + ih, 0x00000000, 0x50000000);
        } else {
            g.fill(ix, iy, ix + iw, iy + ih, theme.flatColor());
        }
    }

    /** Tiles a texture; textures bigger than {@code maxTile} are shrunk so the pattern stays readable. */
    public static void tileTexture(GuiGraphicsExtractor g, Identifier texture, int texW, int texH, int maxTile,
                                   int x, int y, int w, int h) {
        int tileH = Tiles.tileSize(texH, maxTile);
        int tileW = Math.max(1, texW * tileH / texH);
        Tiles.tile(g, texture, texW, texH, tileW, tileH, x, y, w, h);
    }

    /** Darker band behind the title so white text stays readable on every background. */
    public static void drawHeaderBand(GuiGraphicsExtractor g, int x, int y, int w, int h, Theme theme) {
        if (h <= 2) {
            return;
        }
        g.fill(x, y, x + w, y + h, theme.background() == null ? 0xB0373737 : 0x70000000);
        g.fill(x, y + h, x + w, y + h + 1, theme.frameDark());
        g.fill(x, y + h + 1, x + w, y + h + 2, (theme.frameLight() & 0xFFFFFF) | 0x80000000);
    }

    /** Sunken area the list sits in, like an inventory slot. */
    public static void drawWell(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0x70000000);
        g.fill(x - 1, y - 1, x + w, y, 0x90000000);
        g.fill(x - 1, y, x, y + h, 0x90000000);
        g.fill(x, y + h, x + w + 1, y + h + 1, 0x40FFFFFF);
        g.fill(x + w, y - 1, x + w + 1, y + h, 0x40FFFFFF);
    }

    /** One waystone entry with the theme's own fill. {@code barColor} 0 = no group colour bar. */
    public static void drawEntry(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered, boolean active, Theme theme, int barColor) {
        g.fill(x, y, x + w, y + h, hovered && active ? theme.entryHover() : theme.entry());
        drawEntryFrame(g, x, y, w, h, hovered, active, theme.accent(), barColor);
    }

    /** Bevel, group colour bar, hover outline and the "not available" shade on top of an entry fill. */
    public static void drawEntryFrame(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered, boolean active, int accent, int barColor) {
        g.fill(x, y, x + w, y + 1, 0x40FFFFFF);
        g.fill(x, y + 1, x + 1, y + h, 0x28FFFFFF);
        g.fill(x, y + h - 1, x + w, y + h, 0x60000000);
        g.fill(x + w - 1, y, x + w, y + h - 1, 0x40000000);
        if (barColor != 0) {
            g.fill(x + 1, y + 1, x + 4, y + h - 1, 0xFF000000 | barColor);
            g.fill(x + 1, y + 1, x + 4, y + 2, 0x50FFFFFF);
        }
        if (hovered && active) {
            g.outline(x, y, w, h, accent);
        }
        if (!active) {
            g.fill(x, y, x + w, y + h, 0x70000000);
        }
    }
}
