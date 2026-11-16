package dev.waystonescustomizations.theme;

import dev.waystonescustomizations.custom.CustomImages;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * All built-in themes. Backgrounds reference the block textures of the game itself,
 * so they automatically follow whatever resource pack the player uses.
 * Pictures in config/waystonescustomizations/hintergruende become extra themes in the "custom" category.
 */
public final class Themes {
    public static final String VANILLA_ID = "vanilla";
    public static final String CUSTOM_PREFIX = "custom:";
    /** Order of the categories in the theme list. */
    public static final List<String> CATEGORIES = List.of("classic", "wood", "stone", "ocean", "nether", "end", "special", "custom");

    private static final List<Theme> BUILT_IN = new ArrayList<>();
    private static String category = "classic";

    static {
        add(VANILLA_ID, null, 0, 0, 0, 0xFFFFFFFF, 0, 0, "lodestone");
        // Classic inventory look, no texture
        add("inventory", null, 0xFFC6C6C6, 0xFFFFFFFF, 0xFF555555, 0xFFFFFFFF, 0xC0373737, 0xD05A5A5A, "chest");

        category = "wood";
        // id, block texture, frame light, frame dark, accent, icon item
        wood("oak", "oak_planks", 0xFFC8A46A, 0xFF4A3520, 0xFFFFD27A, "oak_sapling");
        wood("spruce", "spruce_planks", 0xFF9C7448, 0xFF2E2014, 0xFFE8B878, "spruce_sapling");
        wood("birch", "birch_planks", 0xFFF2E7B4, 0xFF7A6C40, 0xFFFFF4C0, "birch_sapling");
        wood("jungle", "jungle_planks", 0xFFD0946A, 0xFF4A2A18, 0xFFFFC08A, "jungle_sapling");
        wood("acacia", "acacia_planks", 0xFFE0824A, 0xFF5A2810, 0xFFFFA870, "acacia_sapling");
        wood("dark_oak", "dark_oak_planks", 0xFF7A5634, 0xFF1C120A, 0xFFE0A860, "dark_oak_sapling");
        wood("pale_oak", "pale_oak_planks", 0xFFF4EEEA, 0xFF8C8480, 0xFFFFFFFF, "pale_oak_sapling");
        wood("cherry", "cherry_planks", 0xFFF8D2CC, 0xFF8A4A50, 0xFFFFB8D0, "cherry_sapling");
        wood("mangrove", "mangrove_planks", 0xFFB8584C, 0xFF3C1410, 0xFFFF9A7A, "mangrove_propagule");
        wood("bamboo", "bamboo_planks", 0xFFF4E080, 0xFF7A6420, 0xFFFFF09A, "bamboo");
        wood("bookshelf", "bookshelf", 0xFFC8A46A, 0xFF3A2814, 0xFFFFE08A, "enchanted_book");

        category = "stone";
        stone("stone_bricks", "stone_bricks", 0xFFB8B8B8, 0xFF3A3A3A, 0xFFFFFFFF, "stone_bricks");
        stone("cobblestone", "cobblestone", 0xFFA8A8A8, 0xFF2E2E2E, 0xFFFFFFFF, "cobblestone");
        stone("mossy", "mossy_stone_bricks", 0xFFA8B890, 0xFF2E3A24, 0xFFB6F07A, "moss_block");
        stone("deepslate", "deepslate_tiles", 0xFF6A6A70, 0xFF141418, 0xFF7AD8FF, "deepslate_tiles");
        stone("tuff", "tuff_bricks", 0xFF9A9C90, 0xFF2C2E28, 0xFFE0F0A0, "tuff_bricks");
        stone("calcite", "calcite", 0xFFFFFFFF, 0xFF9A9A9A, 0xFFE0E8FF, "calcite");
        stone("bricks", "bricks", 0xFFD08A70, 0xFF4A2014, 0xFFFFB090, "brick");
        stone("mud", "mud_bricks", 0xFFC0A080, 0xFF4A3828, 0xFFF0D0A0, "mud_bricks");
        stone("sandstone", "sandstone", 0xFFF2E4B0, 0xFF8A7440, 0xFFFFF0B0, "sand");
        stone("red_sandstone", "red_sandstone", 0xFFE0904A, 0xFF6A3010, 0xFFFFB070, "red_sand");
        stone("quartz", "quartz_block_side", 0xFFFFFFFF, 0xFF9A948C, 0xFFFFFFFF, "quartz");

        category = "ocean";
        stone("prismarine", "prismarine_bricks", 0xFF8AD8C8, 0xFF205A4E, 0xFF9AFFF0, "prismarine_shard");
        stone("dark_prismarine", "dark_prismarine", 0xFF5A9A80, 0xFF102A22, 0xFF7AF0C8, "heart_of_the_sea");
        stone("snow", "snow", 0xFFFFFFFF, 0xFF8AA0B0, 0xFFC8F0FF, "snowball");
        stone("ice", "packed_ice", 0xFFC0DCFF, 0xFF4A6A9A, 0xFFB0E8FF, "packed_ice");
        stone("blue_ice", "blue_ice", 0xFFA0C8FF, 0xFF2A4A8A, 0xFFB0E0FF, "blue_ice");

        category = "nether";
        stone("nether", "nether_bricks", 0xFF6A3438, 0xFF1A0A0C, 0xFFFF6A50, "netherrack");
        stone("red_nether", "red_nether_bricks", 0xFFA0282C, 0xFF2A0606, 0xFFFF5A4A, "nether_wart");
        stone("blackstone", "polished_blackstone_bricks", 0xFF5A5060, 0xFF140E16, 0xFFFFC84A, "gilded_blackstone");
        wood("crimson", "crimson_planks", 0xFFA04870, 0xFF3A1024, 0xFFFF7AB0, "crimson_fungus");
        wood("warped", "warped_planks", 0xFF4AA894, 0xFF123A34, 0xFF6AFFE0, "warped_fungus");
        stone("obsidian", "obsidian", 0xFF4A3A6A, 0xFF08040E, 0xFFB07AFF, "obsidian");
        stone("crying_obsidian", "crying_obsidian", 0xFF7A3AB0, 0xFF100420, 0xFFD08AFF, "crying_obsidian");

        category = "end";
        stone("end", "end_stone_bricks", 0xFFF4F4C0, 0xFF6A6A40, 0xFFFFFFC8, "ender_eye");
        stone("purpur", "purpur_block", 0xFFC898C8, 0xFF4A2A4A, 0xFFF0B8F0, "chorus_fruit");

        category = "special";
        stone("copper", "cut_copper", 0xFFE89A70, 0xFF6A3018, 0xFFFFC090, "copper_ingot");
        stone("oxidized", "oxidized_cut_copper", 0xFF6ACAA8, 0xFF1E5444, 0xFF8AFFD8, "oxidized_copper");
        stone("gold", "gold_block", 0xFFFFF27A, 0xFF8A6A10, 0xFFFFFFA0, "gold_ingot");
        stone("emerald", "emerald_block", 0xFF7AF0A0, 0xFF0E5A2A, 0xFFA0FFC0, "emerald");
        stone("lapis", "lapis_block", 0xFF6A8AE0, 0xFF101E5A, 0xFF9AB8FF, "lapis_lazuli");
        stone("amethyst", "amethyst_block", 0xFFC8A0F0, 0xFF3E2460, 0xFFE0B8FF, "amethyst_shard");
        stone("resin", "resin_bricks", 0xFFF0A040, 0xFF6A3008, 0xFFFFC860, "resin_clump");
        stone("honey", "honeycomb_block", 0xFFFFD060, 0xFF7A5010, 0xFFFFE890, "honeycomb");
    }

    private Themes() {
    }

    private static void wood(String id, String block, int light, int dark, int accent, String icon) {
        add(id, block, 0, light, dark, accent, 0xB0201408, 0xC8402A14, icon);
    }

    private static void stone(String id, String block, int light, int dark, int accent, String icon) {
        add(id, block, 0, light, dark, accent, 0xB0101014, 0xC8303038, icon);
    }

    private static void add(String id, String block, int flat, int light, int dark, int accent, int entry, int entryHover, String icon) {
        Identifier bg = block == null ? null : Identifier.withDefaultNamespace("textures/block/" + block + ".png");
        BUILT_IN.add(new Theme(id, category, bg, 16, 16, flat, light, dark, accent, entry, entryHover,
                Identifier.withDefaultNamespace(icon), null));
    }

    /** Built-in themes plus one theme per picture in the backgrounds folder. */
    public static List<Theme> all() {
        List<Theme> list = new ArrayList<>(BUILT_IN);
        for (CustomImages.Image image : CustomImages.get(CustomImages.Kind.BACKGROUNDS)) {
            list.add(custom(image));
        }
        return list;
    }

    private static Theme custom(CustomImages.Image image) {
        return new Theme(CUSTOM_PREFIX + image.key(), "custom", image.id(), image.width(), image.height(), 0,
                0xFFB8B8B8, 0xFF2A2A2A, 0xFFFFFFFF, 0xB0101014, 0xC8303038,
                Identifier.withDefaultNamespace("painting"), image.name());
    }

    public static Theme byId(String id) {
        if (id.startsWith(CUSTOM_PREFIX)) {
            var image = CustomImages.find(CustomImages.Kind.BACKGROUNDS, id.substring(CUSTOM_PREFIX.length()));
            if (image.isPresent()) {
                return custom(image.get());
            }
        }
        for (Theme theme : BUILT_IN) {
            if (theme.id().equals(id)) {
                return theme;
            }
        }
        return byId("oak");
    }
}
