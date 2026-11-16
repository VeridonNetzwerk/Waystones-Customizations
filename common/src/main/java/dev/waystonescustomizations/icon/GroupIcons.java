package dev.waystonescustomizations.icon;

import dev.waystonescustomizations.WaystonesCustomizations;
import dev.waystonescustomizations.custom.CustomImages;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every kind of group icon: our pixel-art sprites, Waystones' own sprites, item icons and pictures
 * from the icons folder. Also knows the tabs of the icon picker.
 */
public final class GroupIcons {
    public static final String SPRITE_PREFIX = "icons/";

    /** Tabs of the icon picker, in order. */
    public enum Tab {
        PLACES("places", "home"),
        NATURE("nature", "grass_block"),
        RESOURCES("resources", "diamond"),
        DANGER("danger", "skull"),
        WORLDS("worlds", "nether_portal"),
        WAYSTONES("waystones", null),
        ITEMS("items", null),
        CUSTOM("custom", "folder");

        public final String id;
        private final @Nullable String iconName;

        Tab(String id, @Nullable String iconName) {
            this.id = id;
            this.iconName = iconName;
        }

        public Component title() {
            return Component.translatable("waystonescustomizations.tab." + id);
        }

        /** Icon drawn on the tab itself. */
        public Identifier tabIcon() {
            if (iconName != null) {
                return sprite(iconName);
            }
            if (this == ITEMS) {
                return ItemIcons.toIcon(Identifier.withDefaultNamespace("compass"));
            }
            List<Identifier> waystones = icons(this);
            return waystones.isEmpty() ? sprite("home") : waystones.getFirst();
        }
    }

    private static @Nullable Map<String, List<Identifier>> builtin;
    private static List<Identifier> waystonesIcons = List.of();

    private GroupIcons() {
    }

    public static Identifier sprite(String name) {
        return Identifier.fromNamespaceAndPath(WaystonesCustomizations.MOD_ID, SPRITE_PREFIX + name);
    }

    /** Called with the icon list of Waystones itself, before we add ours. */
    public static void rememberWaystonesIcons(List<Identifier> icons) {
        waystonesIcons = List.copyOf(icons);
    }

    /** Our pixel-art icons by category (from assets/waystonescustomizations/icon_index.txt). */
    private static Map<String, List<Identifier>> builtin() {
        if (builtin == null) {
            Map<String, List<Identifier>> map = new LinkedHashMap<>();
            try (InputStream in = GroupIcons.class.getResourceAsStream("/assets/" + WaystonesCustomizations.MOD_ID + "/icon_index.txt")) {
                if (in != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.trim().split(" ");
                        if (parts.length == 2 && !parts[0].equals("ui")) {
                            map.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(sprite(parts[1]));
                        }
                    }
                }
            } catch (Exception e) {
                WaystonesCustomizations.LOGGER.warn("Could not read the icon index", e);
            }
            builtin = map;
        }
        return builtin;
    }

    /** All our pixel-art icons (offered by Waystones' own icon button too). */
    public static List<Identifier> allBuiltin() {
        List<Identifier> list = new ArrayList<>();
        builtin().values().forEach(list::addAll);
        return list;
    }

    public static List<Identifier> customIcons() {
        return CustomImages.get(CustomImages.Kind.ICONS).stream().map(CustomImages.Image::id).toList();
    }

    public static List<Identifier> icons(Tab tab) {
        return switch (tab) {
            case WAYSTONES -> waystonesIcons;
            case ITEMS -> ItemIcons.all();
            case CUSTOM -> customIcons();
            default -> builtin().getOrDefault(tab.id, List.of());
        };
    }

    public static Tab tabOf(@Nullable Identifier icon) {
        if (icon == null) {
            return Tab.PLACES;
        }
        for (Tab tab : Tab.values()) {
            if (icons(tab).contains(icon)) {
                return tab;
            }
        }
        return Tab.PLACES;
    }

    /** Name shown for any group icon. Never shows a raw translation key. */
    public static Component displayName(Identifier icon) {
        if (ItemIcons.isItemIcon(icon)) {
            return ItemIcons.item(icon).map(item -> new ItemStack(item).getHoverName()).orElse(Component.literal(pretty(icon.getPath())));
        }
        if (CustomImages.isCustomIcon(icon)) {
            return CustomImages.icon(icon).<Component>map(image -> Component.literal(image.name()))
                    .orElse(Component.literal(pretty(icon.getPath())));
        }
        String dotted = icon.getPath().replace('/', '.');
        Language language = Language.getInstance();
        for (String key : new String[]{
                "waystonescustomizations.icon." + icon.getNamespace() + "." + dotted,
                icon.getNamespace() + "." + dotted,
                "gui.waystones.group_icon." + dotted}) {
            if (language.has(key)) {
                return Component.translatable(key);
            }
        }
        return Component.literal(pretty(icon.getPath()));
    }

    /** "groups/player_homes" -> "Player Homes" */
    private static String pretty(String path) {
        String last = path.substring(path.lastIndexOf('/') + 1).replace('_', ' ');
        StringBuilder out = new StringBuilder();
        for (String word : last.split(" ")) {
            if (!word.isEmpty()) {
                out.append(out.isEmpty() ? "" : " ").append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
        }
        return out.toString();
    }

    /** True for icons Waystones cannot draw itself (items and pictures from the icons folder). */
    public static boolean needsCustomDraw(Identifier icon) {
        return ItemIcons.isItemIcon(icon) || CustomImages.isCustomIcon(icon);
    }

    /** Draws icons Waystones cannot draw itself, see {@link #needsCustomDraw}. */
    public static void drawCustom(GuiGraphicsExtractor g, Identifier icon, int x, int y, int w, int h) {
        if (ItemIcons.isItemIcon(icon)) {
            ItemIcons.drawItem(g, icon, x, y, w, h);
            return;
        }
        CustomImages.icon(icon).ifPresent(image ->
                g.blit(RenderPipelines.GUI_TEXTURED, image.id(), x, y, 0, 0, w, h, image.width(), image.height(), image.width(), image.height()));
    }

    /** Draws any group icon. */
    public static void draw(GuiGraphicsExtractor g, Identifier icon, int x, int y, int w, int h) {
        if (needsCustomDraw(icon)) {
            drawCustom(g, icon, x, y, w, h);
        } else {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x, y, w, h);
        }
    }
}
