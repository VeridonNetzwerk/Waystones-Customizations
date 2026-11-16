package dev.waystonescustomizations.style;

import dev.waystonescustomizations.custom.CustomImages;
import dev.waystonescustomizations.theme.Theme;
import dev.waystonescustomizations.theme.ThemeRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Background of one waystone entry, stored as a short text:
 * <ul>
 *     <li>{@code auto} - tinted in the group colour (plain theme look without a group)</li>
 *     <li>{@code theme} - always the plain theme look</li>
 *     <li>{@code color:RRGGBB}</li>
 *     <li>{@code block:<block texture name>} - e.g. {@code block:oak_planks}</li>
 *     <li>{@code custom:<file key>} - picture from config/waystonescustomizations/hintergruende</li>
 * </ul>
 */
public final class EntryBackground {
    public static final String AUTO = "auto";
    private static final java.util.Map<String, Identifier> BLOCK_TEXTURES = new java.util.HashMap<>();
    public static final String THEME = "theme";

    /** Minecraft's 16 dye colours. */
    public static final List<String> COLORS = List.of(
            "color:F9FFFE", "color:9D9D97", "color:474F52", "color:1D1D21",
            "color:B02E26", "color:F9801D", "color:FED83D", "color:80C71F",
            "color:5E7C16", "color:169C9C", "color:3AB3DA", "color:3C44AA",
            "color:8932B8", "color:C74EBD", "color:F38BAA", "color:835432");

    private static final String[] COLOR_NAMES = {
            "white", "light_gray", "gray", "black", "red", "orange", "yellow", "lime",
            "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink", "brown"};

    /** Block textures offered as backgrounds (none of them animated). */
    public static final List<String> BLOCKS = List.of(
            "oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks",
            "pale_oak_planks", "mangrove_planks", "cherry_planks", "bamboo_planks", "crimson_planks", "warped_planks",
            "oak_log", "cherry_log", "bookshelf", "crafting_table_front", "hay_block_side", "pumpkin_side",
            "melon_side", "moss_block", "dirt", "mycelium_top", "sand", "gravel", "clay",
            "cobblestone", "mossy_cobblestone", "stone_bricks", "mossy_stone_bricks", "bricks", "mud_bricks",
            "deepslate_tiles", "polished_deepslate", "tuff_bricks", "calcite", "sandstone_top", "red_sandstone_top",
            "terracotta", "quartz_block_side", "snow", "packed_ice", "blue_ice", "prismarine_bricks", "dark_prismarine",
            "netherrack", "nether_bricks", "red_nether_bricks", "soul_sand", "shroomlight", "polished_blackstone_bricks",
            "obsidian", "crying_obsidian", "end_stone_bricks", "purpur_block", "iron_block", "gold_block",
            "copper_block", "cut_copper", "oxidized_cut_copper", "diamond_block", "emerald_block", "lapis_block",
            "amethyst_block", "honeycomb_block", "resin_bricks", "glowstone", "tnt_side", "red_mushroom_block",
            "white_wool", "red_wool", "lime_wool", "blue_wool");

    private EntryBackground() {
    }

    /** True if this background replaces the theme's normal entry look. */
    public static boolean overridesTheme(String value, int groupColor) {
        if (value.equals(THEME)) {
            return false;
        }
        if (value.equals(AUTO)) {
            return groupColor != 0;
        }
        return true;
    }

    /** Fills the entry. Only call if {@link #overridesTheme} is true. */
    public static void fill(GuiGraphicsExtractor g, String value, int groupColor, int x, int y, int w, int h, boolean hovered) {
        if (value.equals(AUTO)) {
            tint(g, groupColor, x, y, w, h, hovered);
            return;
        }
        Optional<Integer> color = color(value);
        if (color.isPresent()) {
            tint(g, color.get(), x, y, w, h, hovered);
            return;
        }
        if (drawTexture(g, value, x, y, w, h, h)) {
            // keep white text readable on bright textures
            g.fill(x, y, x + w, y + h, hovered ? 0x88000000 : 0xA8000000);
            if (hovered) {
                g.fill(x, y, x + w, y + h, 0x18FFFFFF);
            }
        } else {
            g.fill(x, y, x + w, y + h, 0xB0101014);
        }
    }

    private static void tint(GuiGraphicsExtractor g, int rgb, int x, int y, int w, int h, boolean hovered) {
        int fill = mix(rgb, 0x000000, hovered ? 0.62f : 0.74f);
        int top = mix(rgb, 0x000000, hovered ? 0.5f : 0.62f);
        g.fillGradient(x, y, x + w, y + h, 0xE8000000 | top, 0xE8000000 | fill);
    }

    /** Draws a texture background (block or picture) tiled into the rectangle. Returns false for colours/auto. */
    public static boolean drawTexture(GuiGraphicsExtractor g, String value, int x, int y, int w, int h, int maxTile) {
        if (value.startsWith("block:")) {
            Identifier texture = BLOCK_TEXTURES.computeIfAbsent(value, v -> Identifier.tryParse(blockTexture(v.substring(6))));
            if (texture == null) {
                return false;
            }
            ThemeRenderer.tileTexture(g, texture, 16, 16, Math.max(16, maxTile), x, y, w, h);
            return true;
        }
        if (value.startsWith("custom:")) {
            Optional<CustomImages.Image> image = CustomImages.find(CustomImages.Kind.BACKGROUNDS, value.substring(7));
            if (image.isEmpty()) {
                return false;
            }
            CustomImages.Image img = image.get();
            if (img.width() >= img.height() * 4) {
                // a wide banner: stretch over the whole entry
                g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, img.id(), x, y, 0, 0, w, h,
                        img.width(), img.height(), img.width(), img.height());
            } else {
                ThemeRenderer.tileTexture(g, img.id(), img.width(), img.height(), maxTile, x, y, w, h);
            }
            return true;
        }
        return false;
    }

    private static String blockTexture(String block) {
        String ns = "minecraft";
        int colon = block.indexOf(':');
        if (colon > 0) {
            ns = block.substring(0, colon);
            block = block.substring(colon + 1);
        }
        return ns + ":textures/block/" + block + ".png";
    }

    /** Small preview square used in the picker. */
    public static void drawSwatch(GuiGraphicsExtractor g, String value, int groupColor, Theme theme, int x, int y, int size) {
        if (value.equals(THEME) || (value.equals(AUTO) && groupColor == 0)) {
            if (theme.background() != null) {
                ThemeRenderer.tileTexture(g, theme.background(), theme.texWidth(), theme.texHeight(), size, x, y, size, size);
                g.fill(x, y, x + size, y + size, 0x50000000);
            } else {
                g.fill(x, y, x + size, y + size, theme.isVanilla() ? 0xFF6F6F6F : theme.flatColor());
            }
            return;
        }
        if (value.equals(AUTO)) {
            g.fill(x, y, x + size, y + size, 0xFF000000 | mix(groupColor, 0, 0.6f));
            return;
        }
        Optional<Integer> color = color(value);
        if (color.isPresent()) {
            g.fill(x, y, x + size, y + size, 0xFF000000 | color.get());
            return;
        }
        if (!drawTexture(g, value, x, y, size, size, size)) {
            g.fill(x, y, x + size, y + size, 0xFF000000);
        }
    }

    public static Optional<Integer> color(String value) {
        if (!value.startsWith("color:")) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.substring(6), 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Component name(String value) {
        if (value.equals(AUTO)) {
            return Component.translatable("waystonescustomizations.background.auto");
        }
        if (value.equals(THEME)) {
            return Component.translatable("waystonescustomizations.background.theme");
        }
        int colorIndex = COLORS.indexOf(value.toUpperCase(Locale.ROOT).replace("COLOR:", "color:"));
        if (colorIndex >= 0) {
            return Component.translatable("color.minecraft." + COLOR_NAMES[colorIndex]);
        }
        if (value.startsWith("color:")) {
            return Component.literal("#" + value.substring(6));
        }
        if (value.startsWith("block:")) {
            String block = value.substring(6);
            String base = block.replaceAll("_(side|top|front)$", "");
            String key = "block.minecraft." + base;
            return net.minecraft.locale.Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(base);
        }
        if (value.startsWith("custom:")) {
            return CustomImages.find(CustomImages.Kind.BACKGROUNDS, value.substring(7))
                    .<Component>map(image -> Component.literal(image.name()))
                    .orElse(Component.literal(value.substring(7)));
        }
        return Component.literal(value);
    }

    private static int mix(int rgb, int target, float amount) {
        int r = (rgb >> 16) & 0xFF;
        int gr = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int tr = (target >> 16) & 0xFF;
        int tg = (target >> 8) & 0xFF;
        int tb = target & 0xFF;
        r = Math.round(r + (tr - r) * amount);
        gr = Math.round(gr + (tg - gr) * amount);
        b = Math.round(b + (tb - b) * amount);
        return (r << 16) | (gr << 8) | b;
    }
}
