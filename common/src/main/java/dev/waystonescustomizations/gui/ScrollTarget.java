package dev.waystonescustomizations.gui;

/**
 * Our widgets that want the mouse wheel. Minecraft's container screens never pass scrolling on
 * to their widgets, so a mixin does it for widgets marked with this interface.
 */
public interface ScrollTarget {
    boolean waystonescustomizations$scroll(double mouseX, double mouseY, double scrollY);
}
