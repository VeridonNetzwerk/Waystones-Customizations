package dev.waystonescustomizations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.waystonescustomizations.theme.Theme;
import dev.waystonescustomizations.theme.Themes;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Client config, stored as config/waystonescustomizations.json. */
public final class ThemeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ThemeConfig instance;

    /** Id of the active theme, see {@link Themes}. */
    public String theme = "oak";
    /** Show the group icons of a waystone inside its list entry. */
    public boolean showGroupIcons = true;
    /** Show a coloured bar in the group colour on the left of each entry. */
    public boolean showGroupColorBar = true;
    /** Strength of the dark layer over the background texture, 0-255. */
    public int backgroundDarkness = 110;

    /** Theme shown while the mouse is over it in the theme list (not saved). */
    private transient @Nullable Theme preview;

    public static ThemeConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public Theme activeTheme() {
        return preview != null ? preview : Themes.byId(theme);
    }

    public Theme savedTheme() {
        return Themes.byId(theme);
    }

    public void setPreview(@Nullable Theme preview) {
        this.preview = preview;
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(WaystonesCustomizations.MOD_ID + ".json");
    }

    private static ThemeConfig load() {
        Path path = file();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ThemeConfig config = GSON.fromJson(reader, ThemeConfig.class);
                if (config != null) {
                    config.backgroundDarkness = Math.clamp(config.backgroundDarkness, 0, 255);
                    return config;
                }
            } catch (Exception e) {
                WaystonesCustomizations.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }
        ThemeConfig config = new ThemeConfig();
        config.save();
        return config;
    }

    public void save() {
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
