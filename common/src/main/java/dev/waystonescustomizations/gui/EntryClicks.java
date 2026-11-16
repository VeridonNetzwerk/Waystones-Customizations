package dev.waystonescustomizations.gui;

import dev.waystonescustomizations.gui.popup.BackgroundPopup;
import dev.waystonescustomizations.gui.popup.Popups;
import dev.waystonescustomizations.icon.WaystoneGroupLookup;
import net.blay09.mods.waystones.api.Waystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

/**
 * Remembers which waystone entry the mouse is over (set while drawing), so a right click on it
 * or a click on its brush opens the background picker instead of teleporting.
 */
public final class EntryClicks {
    public static final int BRUSH_SIZE = 12;

    private static @Nullable AbstractWidget button;
    private static @Nullable Waystone waystone;
    private static int brushX;
    private static int brushY;

    private EntryClicks() {
    }

    public static void track(AbstractWidget entryButton, Waystone entryWaystone, int brushLeft, int brushTop) {
        button = entryButton;
        waystone = entryWaystone;
        brushX = brushLeft;
        brushY = brushTop;
    }

    /** Drops the remembered entry (its screen was closed). */
    public static void forget() {
        button = null;
        waystone = null;
    }

    public static boolean isOverBrush(double mouseX, double mouseY) {
        return mouseX >= brushX && mouseX < brushX + BRUSH_SIZE && mouseY >= brushY && mouseY < brushY + BRUSH_SIZE;
    }

    /** @return true if the click was used */
    public static boolean click(Screen screen, MouseButtonEvent event) {
        if (button == null || waystone == null || !button.visible
                || event.x() < button.getX() || event.x() >= button.getRight()
                || event.y() < button.getY() || event.y() >= button.getBottom()) {
            return false;
        }
        boolean rightClick = event.button() == 1;
        boolean brush = event.button() == 0 && isOverBrush(event.x(), event.y());
        if (!rightClick && !brush) {
            return false;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        Popups.open(screen, new BackgroundPopup(waystone, WaystoneGroupLookup.groupsOf(waystone),
                (int) event.x(), (int) event.y(), screen.width, screen.height));
        return true;
    }
}
