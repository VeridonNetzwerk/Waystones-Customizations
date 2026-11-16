package dev.waystonescustomizations.compat;

import java.nio.file.Path;

/** Things that moved between Minecraft versions (26.3 variant). */
public final class Platform {
    private Platform() {
    }

    public static void openFolder(Path path) {
        com.mojang.blaze3d.Blaze3D.openPath(path);
    }
}
