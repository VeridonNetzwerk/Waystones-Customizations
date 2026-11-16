package dev.waystonescustomizations.style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.waystonescustomizations.WaystonesCustomizations;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Background chosen per waystone and per group, stored only on this computer in
 * config/waystonescustomizations/styles.json. A waystone uses its own choice, else the one of its
 * first group, else {@link EntryBackground#AUTO}.
 */
public final class EntryStyles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static EntryStyles instance;

    /** waystone uuid -> background */
    private Map<String, String> waystones = new HashMap<>();
    /** group id -> background */
    private Map<String, String> groups = new HashMap<>();
    /** waystone uuid -> text colour ("color:RRGGBB") */
    private Map<String, String> waystoneText = new HashMap<>();
    /** group id -> text colour ("color:RRGGBB") */
    private Map<String, String> groupText = new HashMap<>();

    public @Nullable String waystoneText(UUID id) {
        return waystoneText.get(id.toString());
    }

    public @Nullable String groupText(Identifier id) {
        return groupText.get(id.toString());
    }

    public void setWaystoneText(UUID id, @Nullable String color) {
        put(waystoneText, id.toString(), color);
    }

    public void setGroupText(Identifier id, @Nullable String color) {
        put(groupText, id.toString(), color);
    }

    private void put(Map<String, String> map, String key, @Nullable String value) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
        save();
    }

    /** Text colour (RGB) of an entry: its own, else its first group's, else null (Waystones decides). */
    public @Nullable Integer resolveText(Waystone waystone, List<WaystoneGroup> groups) {
        String value = waystoneText(waystone.getWaystoneUid());
        if (value == null && !groups.isEmpty()) {
            value = groupText(groups.getFirst().identifier());
        }
        return value == null ? null : EntryBackground.color(value).orElse(null);
    }

    public static EntryStyles get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public @Nullable String waystone(UUID id) {
        return waystones.get(id.toString());
    }

    public @Nullable String group(Identifier id) {
        return groups.get(id.toString());
    }

    public void setWaystone(UUID id, @Nullable String background) {
        if (background == null) {
            waystones.remove(id.toString());
        } else {
            waystones.put(id.toString(), background);
        }
        save();
    }

    public void setGroup(Identifier id, @Nullable String background) {
        if (background == null) {
            groups.remove(id.toString());
        } else {
            groups.put(id.toString(), background);
        }
        save();
    }

    /** @param groups the waystone's visible groups, first one counts */
    public String resolve(Waystone waystone, List<WaystoneGroup> groups) {
        String own = waystone(waystone.getWaystoneUid());
        if (own != null) {
            return own;
        }
        if (!groups.isEmpty()) {
            String group = group(groups.getFirst().identifier());
            if (group != null) {
                return group;
            }
        }
        return EntryBackground.AUTO;
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(WaystonesCustomizations.MOD_ID).resolve("styles.json");
    }

    private static EntryStyles load() {
        Path path = file();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                EntryStyles styles = GSON.fromJson(reader, EntryStyles.class);
                if (styles != null) {
                    if (styles.waystones == null) {
                        styles.waystones = new HashMap<>();
                    }
                    if (styles.groups == null) {
                        styles.groups = new HashMap<>();
                    }
                    if (styles.waystoneText == null) {
                        styles.waystoneText = new HashMap<>();
                    }
                    if (styles.groupText == null) {
                        styles.groupText = new HashMap<>();
                    }
                    return styles;
                }
            } catch (Exception e) {
                WaystonesCustomizations.LOGGER.warn("Could not read {}", path, e);
            }
        }
        return new EntryStyles();
    }

    private void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            WaystonesCustomizations.LOGGER.warn("Could not save {}", path, e);
        }
    }
}
