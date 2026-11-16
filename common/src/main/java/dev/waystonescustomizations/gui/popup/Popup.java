package dev.waystonescustomizations.gui.popup;

import dev.waystonescustomizations.gui.UiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** A small window drawn above a screen, with an optional scrolling content area ("view"). */
public abstract class Popup {
    protected final Font font = Minecraft.getInstance().font;
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    /** Scrolling area inside the popup. */
    protected int viewX;
    protected int viewY;
    protected int viewW;
    protected int viewH;
    protected int contentHeight;
    protected int scroll;

    public abstract void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float a);

    public void mouseClicked(double mouseX, double mouseY, int button) {
    }

    public void mouseScrolled(double mouseX, double mouseY, double scrollY) {
        scroll = Math.clamp(scroll - (int) Math.round(scrollY * 18), 0, maxScroll());
    }

    public void onClose() {
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected boolean inView(double mouseX, double mouseY) {
        return mouseX >= viewX && mouseX < viewX + viewW && mouseY >= viewY && mouseY < viewY + viewH;
    }

    protected int maxScroll() {
        return Math.max(0, contentHeight - viewH);
    }

    /** Keeps the popup on screen. */
    protected void clampToScreen(int screenWidth, int screenHeight) {
        x = Math.clamp(x, 2, Math.max(2, screenWidth - width - 2));
        y = Math.clamp(y, 2, Math.max(2, screenHeight - height - 2));
    }

    protected void drawScrollbar(GuiGraphicsExtractor g) {
        if (maxScroll() <= 0) {
            return;
        }
        int trackX = viewX + viewW + 1;
        g.fill(trackX, viewY, trackX + 3, viewY + viewH, 0xFF373737);
        int thumbH = Math.max(10, viewH * viewH / contentHeight);
        int thumbY = viewY + (viewH - thumbH) * scroll / maxScroll();
        g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, 0xFFFFFFFF);
        g.fill(trackX + 1, thumbY + 1, trackX + 3, thumbY + thumbH, 0xFFA0A0A0);
    }

    protected void drawWindow(GuiGraphicsExtractor g) {
        UiDraw.window(g, x, y, width, height);
    }
}
