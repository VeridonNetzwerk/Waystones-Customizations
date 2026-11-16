package dev.waystonescustomizations.theme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Fills a rectangle with a repeating texture. */
public final class Tiles {
    private Tiles() {
    }

    /**
     * @param tileW size one copy of the texture is drawn at
     * @param tileH size one copy of the texture is drawn at
     */
    public static void tile(GuiGraphicsExtractor g, Identifier texture, int texW, int texH, int tileW, int tileH,
                            int x, int y, int w, int h) {
        for (int ty = 0; ty < h; ty += tileH) {
            int th = Math.min(tileH, h - ty);
            int srcH = Math.max(1, th * texH / tileH);
            for (int tx = 0; tx < w; tx += tileW) {
                int tw = Math.min(tileW, w - tx);
                int srcW = Math.max(1, tw * texW / tileW);
                g.blit(RenderPipelines.GUI_TEXTURED, texture, x + tx, y + ty, 0, 0, tw, th, srcW, srcH, texW, texH);
            }
        }
    }

    /** Tile size for a texture: small textures keep their size, big ones are shrunk to fit {@code maxTile}. */
    public static int tileSize(int texSize, int maxTile) {
        return texSize <= maxTile ? texSize : maxTile;
    }
}
