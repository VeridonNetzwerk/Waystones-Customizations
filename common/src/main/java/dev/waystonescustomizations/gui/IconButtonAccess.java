package dev.waystonescustomizations.gui;

import net.minecraft.resources.Identifier;

/** Implemented (via mixin) by Waystones' group icon button so the icon picker can set the icon. */
public interface IconButtonAccess {
    Identifier waystonescustomizations$getIcon();

    void waystonescustomizations$setIcon(Identifier icon);
}
