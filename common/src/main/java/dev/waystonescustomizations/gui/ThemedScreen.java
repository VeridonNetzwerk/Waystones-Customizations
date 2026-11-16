package dev.waystonescustomizations.gui;

/** Implemented (via mixin) by the Waystones selection screens. */
public interface ThemedScreen {
    /** Panel rectangle as {x, y, width, height} and optionally the y where the header band ends as 5th value. */
    int[] waystonescustomizations$panelBounds();
}
