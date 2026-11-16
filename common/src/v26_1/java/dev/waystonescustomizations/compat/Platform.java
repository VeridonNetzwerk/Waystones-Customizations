package dev.waystonescustomizations.compat;

import java.nio.file.Path;

/** Things that moved between Minecraft versions (26.1.2 / 26.2 variant). */
public final class Platform {
    private Platform() {
    }

    public static void openFolder(Path path) {
        net.minecraft.util.Util.getPlatform().openPath(path);
    }
}
