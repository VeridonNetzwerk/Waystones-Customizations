package dev.waystonescustomizations.icon;

import dev.waystonescustomizations.WaystonesCustomizations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Group icons that show a Minecraft item instead of a texture. An icon is stored by Waystones as a
 * normal identifier: {@code waystonescustomizations:item/<namespace>/<path>}. Wherever Waystones would draw the
 * sprite for such an identifier we draw the item instead, so no texture pack is needed.
 */
public final class ItemIcons {
    private static final String PREFIX = "item/";

    /** Curated, sorted by theme. Items that do not exist in the running version are skipped. */
    private static final String[] CATALOG = {
            // home & base
            "red_bed", "white_bed", "light_blue_bed", "oak_door", "campfire", "crafting_table", "furnace", "chest",
            "barrel", "ender_chest", "anvil", "lantern", "torch", "flower_pot", "painting", "armor_stand",
            // villages, trade, farming
            "bell", "emerald", "wheat", "carrot", "potato", "beetroot", "pumpkin", "melon_slice", "sweet_berries",
            "apple", "bread", "cake", "cookie", "honeycomb", "bee_nest", "egg", "milk_bucket", "hay_block", "composter",
            // mining & resources
            "wooden_pickaxe", "stone_pickaxe", "iron_pickaxe", "diamond_pickaxe", "netherite_pickaxe", "iron_shovel",
            "iron_axe", "coal", "raw_iron", "iron_ingot", "raw_copper", "copper_ingot", "raw_gold", "gold_ingot",
            "redstone", "lapis_lazuli", "diamond", "netherite_ingot", "ancient_debris", "amethyst_shard", "quartz",
            "glowstone_dust", "tnt", "minecart", "rail", "powered_rail",
            // nature & biomes
            "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "cherry_sapling",
            "pale_oak_sapling", "mangrove_propagule", "bamboo", "cactus", "dead_bush", "poppy", "dandelion",
            "sunflower", "brown_mushroom", "red_mushroom", "grass_block", "moss_block", "sand", "snowball",
            "ice", "lily_pad", "kelp", "sea_pickle", "cod", "tropical_fish", "water_bucket", "lava_bucket",
            // exploration
            "compass", "recovery_compass", "map", "spyglass", "clock", "oak_boat", "saddle", "lead", "name_tag",
            "elytra", "firework_rocket", "ender_pearl", "ender_eye", "heart_of_the_sea", "nautilus_shell",
            "trial_key", "ominous_trial_key", "brush", "goat_horn", "echo_shard",
            // combat & dungeons
            "wooden_sword", "iron_sword", "diamond_sword", "netherite_sword", "mace", "bow", "crossbow", "trident",
            "shield", "iron_chestplate", "diamond_helmet", "totem_of_undying", "skeleton_skull",
            "wither_skeleton_skull", "zombie_head", "creeper_head", "dragon_head", "piglin_head", "spawner",
            "bone", "rotten_flesh", "spider_eye", "gunpowder", "string", "slime_ball", "phantom_membrane",
            // magic & brewing
            "enchanting_table", "enchanted_book", "book", "writable_book", "bookshelf", "brewing_stand", "potion",
            "experience_bottle", "golden_apple", "enchanted_golden_apple", "blaze_rod", "ghast_tear", "magma_cream",
            "nether_wart", "breeze_rod", "wind_charge", "heavy_core",
            // nether & end
            "netherrack", "obsidian", "crying_obsidian", "flint_and_steel", "respawn_anchor", "lodestone",
            "crimson_fungus", "warped_fungus", "soul_lantern", "gilded_blackstone", "end_stone", "chorus_fruit",
            "purpur_block", "shulker_shell", "end_crystal", "nether_star", "beacon", "conduit", "dragon_egg",
            // redstone & tech
            "redstone_torch", "repeater", "comparator", "lever", "piston", "observer", "hopper", "dispenser",
            "note_block", "jukebox", "music_disc_cat", "target", "daylight_detector", "copper_bulb",
            // decoration & misc
            "white_banner", "oak_sign", "item_frame", "glow_item_frame", "decorated_pot", "candle", "player_head",
            "bricks", "stone_bricks", "scaffolding", "ladder", "cobweb", "sponge", "barrier", "structure_block",
    };

    private static @Nullable List<Identifier> icons;

    private ItemIcons() {
    }

    public static List<Identifier> all() {
        if (icons == null) {
            List<Identifier> list = new ArrayList<>();
            for (String name : CATALOG) {
                Identifier itemId = Identifier.withDefaultNamespace(name);
                if (BuiltInRegistries.ITEM.containsKey(itemId)) {
                    list.add(toIcon(itemId));
                }
            }
            icons = List.copyOf(list);
        }
        return icons;
    }

    public static Identifier toIcon(Identifier itemId) {
        return Identifier.fromNamespaceAndPath(WaystonesCustomizations.MOD_ID, PREFIX + itemId.getNamespace() + "/" + itemId.getPath());
    }

    public static boolean isItemIcon(@Nullable Identifier icon) {
        return icon != null && icon.getNamespace().equals(WaystonesCustomizations.MOD_ID) && icon.getPath().startsWith(PREFIX);
    }

    public static Optional<Item> item(Identifier icon) {
        String rest = icon.getPath().substring(PREFIX.length());
        int slash = rest.indexOf('/');
        if (slash <= 0) {
            return Optional.empty();
        }
        Identifier itemId = Identifier.tryBuild(rest.substring(0, slash), rest.substring(slash + 1));
        if (itemId == null) {
            return Optional.empty();
        }
        return BuiltInRegistries.ITEM.getOptional(itemId);
    }

    /** Draws an item icon (see {@link #isItemIcon}) scaled to the given size. */
    public static void drawItem(GuiGraphicsExtractor g, Identifier icon, int x, int y, int w, int h) {
        Optional<Item> item = item(icon);
        if (item.isEmpty()) {
            return;
        }
        ItemStack stack = new ItemStack(item.get());
        if (w == 16 && h == 16) {
            g.fakeItem(stack, x, y);
        } else {
            g.pose().pushMatrix();
            g.pose().translate(x, y);
            g.pose().scale(w / 16f, h / 16f);
            g.fakeItem(stack, 0, 0);
            g.pose().popMatrix();
        }
    }
}
