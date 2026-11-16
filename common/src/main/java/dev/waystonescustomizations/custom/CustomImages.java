package dev.waystonescustomizations.custom;

import com.mojang.blaze3d.platform.NativeImage;
import dev.waystonescustomizations.WaystonesCustomizations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import dev.waystonescustomizations.compat.Platform;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Pictures the player drops into config/waystonescustomizations/hintergruende (backgrounds) and
 * config/waystonescustomizations/icons (group icons). Any PNG works, the file name becomes the name.
 */
public final class CustomImages {
    public enum Kind {
        BACKGROUNDS("hintergruende", "custom_bg/"),
        ICONS("icons", "custom/");

        final String folder;
        final String prefix;

        Kind(String folder, String prefix) {
            this.folder = folder;
            this.prefix = prefix;
        }
    }

    /**
     * @param key  file name in lower case without ".png", safe for identifiers
     * @param name file name as the player wrote it, without ".png"
     */
    public record Image(String key, String name, Identifier id, int width, int height) {
    }

    private static final int MAX_SIZE = 512;
    private static final Map<Kind, List<Image>> LOADED = new EnumMap<>(Kind.class);

    private CustomImages() {
    }

    public static Path folder(Kind kind) {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(WaystonesCustomizations.MOD_ID).resolve(kind.folder);
    }

    public static List<Image> get(Kind kind) {
        List<Image> images = LOADED.get(kind);
        if (images == null) {
            images = load(kind);
            LOADED.put(kind, images);
        }
        return images;
    }

    public static Optional<Image> find(Kind kind, String key) {
        for (Image image : get(kind)) {
            if (image.key().equals(key)) {
                return Optional.of(image);
            }
        }
        return Optional.empty();
    }

    /** Finds a custom group icon by its identifier (waystonescustomizations:custom/<key>). */
    public static Optional<Image> icon(Identifier id) {
        if (!isCustomIcon(id)) {
            return Optional.empty();
        }
        return find(Kind.ICONS, id.getPath().substring(Kind.ICONS.prefix.length()));
    }

    public static boolean isCustomIcon(Identifier id) {
        return id.getNamespace().equals(WaystonesCustomizations.MOD_ID) && id.getPath().startsWith(Kind.ICONS.prefix);
    }

    /** Throws away all loaded pictures and reads both folders again. */
    public static void reload() {
        var textures = Minecraft.getInstance().getTextureManager();
        for (List<Image> images : LOADED.values()) {
            for (Image image : images) {
                textures.release(image.id());
            }
        }
        LOADED.clear();
    }

    public static void openFolder(Kind kind) {
        Path path = folder(kind);
        ensureFolder(kind, path);
        Platform.openFolder(path);
    }

    private static void ensureFolder(Kind kind, Path path) {
        try {
            Files.createDirectories(path);
            Path readme = path.resolve("LIESMICH.txt");
            if (!Files.exists(readme)) {
                String text = kind == Kind.ICONS
                        ? "Lege hier PNG-Bilder ab, sie werden zu Gruppen-Icons. Empfohlen: 16x16 Pixel.\n"
                        + "Der Dateiname wird zum Namen. Danach im Spiel auf \"Neu laden\" klicken.\n"
                        + "Hinweis: andere Spieler sehen ein eigenes Icon nur, wenn sie dieselbe Datei haben.\n\n"
                        + "Put PNG pictures here to use them as group icons (16x16 recommended), then click \"Reload\" in game.\n"
                        : "Lege hier PNG-Bilder ab, sie werden zu Hintergruenden (fuer Eintraege und als Design).\n"
                        + "Empfohlen: 16x16 oder 32x32 Pixel, das Bild wird gekachelt. Danach im Spiel auf \"Neu laden\" klicken.\n\n"
                        + "Put PNG pictures here to use them as backgrounds (16x16 or 32x32, tiled), then click \"Reload\" in game.\n";
                Files.writeString(readme, text, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            WaystonesCustomizations.LOGGER.warn("Could not create {}", path, e);
        }
    }

    private static List<Image> load(Kind kind) {
        Path path = folder(kind);
        ensureFolder(kind, path);
        List<Image> result = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(path, "*.{png,PNG}")) {
            for (Path file : files) {
                Image image = loadOne(kind, file);
                if (image != null) {
                    result.add(image);
                }
            }
        } catch (Exception e) {
            WaystonesCustomizations.LOGGER.warn("Could not read {}", path, e);
        }
        result.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return List.copyOf(result);
    }

    private static Image loadOne(Kind kind, Path file) {
        String fileName = file.getFileName().toString();
        String name = fileName.substring(0, fileName.length() - 4);
        String key = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
        Identifier id = Identifier.tryBuild(WaystonesCustomizations.MOD_ID, kind.prefix + key);
        if (id == null) {
            return null;
        }
        try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            int w = image.getWidth();
            int h = image.getHeight();
            if (w > MAX_SIZE || h > MAX_SIZE) {
                WaystonesCustomizations.LOGGER.warn("{} is too big ({}x{}, max {}), skipped", file, w, h, MAX_SIZE);
                image.close();
                return null;
            }
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, image));
            return new Image(key, name, id, w, h);
        } catch (Exception e) {
            WaystonesCustomizations.LOGGER.warn("Could not load picture {}", file, e);
            return null;
        }
    }
}
