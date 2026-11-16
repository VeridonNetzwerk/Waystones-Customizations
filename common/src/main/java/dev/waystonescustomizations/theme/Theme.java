package dev.waystonescustomizations.theme;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * A look for the waystone list.
 *
 * @param id         config id ("custom:<file>" for pictures from the backgrounds folder)
 * @param category   group in the theme list, see {@link Themes#CATEGORIES}
 * @param background tiled texture (e.g. minecraft:textures/block/oak_planks.png), or null for a flat colour
 * @param texWidth   size of the background texture in pixels
 * @param texHeight  size of the background texture in pixels
 * @param flatColor  panel colour when {@code background} is null
 * @param frameLight bright edge of the panel frame (top/left)
 * @param frameDark  dark edge of the panel frame (bottom/right)
 * @param accent     outline colour of hovered entries
 * @param entry      fill of a list entry
 * @param entryHover fill of a hovered entry
 * @param icon       item shown on the theme button
 * @param customName display name for custom themes, null for built-in ones
 */
public record Theme(
        String id,
        String category,
        @Nullable Identifier background,
        int texWidth,
        int texHeight,
        int flatColor,
        int frameLight,
        int frameDark,
        int accent,
        int entry,
        int entryHover,
        Identifier icon,
        @Nullable String customName
) {
    public boolean isVanilla() {
        return id.equals(Themes.VANILLA_ID);
    }

    public Component displayName() {
        return customName != null ? Component.literal(customName) : Component.translatable("waystonescustomizations.theme." + id);
    }
}
