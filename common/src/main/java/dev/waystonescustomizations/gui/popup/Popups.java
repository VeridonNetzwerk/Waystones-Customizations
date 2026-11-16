package dev.waystonescustomizations.gui.popup;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

/**
 * The one popup that may be open at a time. It belongs to a screen; while it is open it gets
 * all mouse and key input of that screen, and a click outside closes it.
 */
public final class Popups {
    private static @Nullable Popup current;
    private static @Nullable Object owner;

    private Popups() {
    }

    public static void open(Object screen, Popup popup) {
        close();
        current = popup;
        owner = screen;
    }

    public static void close() {
        Popup popup = current;
        current = null;
        owner = null;
        if (popup != null) {
            popup.onClose();
        }
    }

    public static @Nullable Popup on(Object screen) {
        return current != null && owner == screen ? current : null;
    }

    public static boolean isOpen(Object screen) {
        return on(screen) != null;
    }

    public static boolean click(Object screen, MouseButtonEvent event) {
        Popup popup = on(screen);
        if (popup == null) {
            return false;
        }
        if (!popup.contains(event.x(), event.y())) {
            close();
        } else {
            popup.mouseClicked(event.x(), event.y(), event.button());
        }
        return true;
    }

    public static boolean scroll(Object screen, double mouseX, double mouseY, double scrollY) {
        Popup popup = on(screen);
        if (popup == null) {
            return false;
        }
        if (popup.contains(mouseX, mouseY)) {
            popup.mouseScrolled(mouseX, mouseY, scrollY);
        }
        return true;
    }

    public static boolean key(Object screen, KeyEvent event) {
        Popup popup = on(screen);
        if (popup == null) {
            return false;
        }
        if (event.isEscape() || event.isConfirmation()) {
            close();
        }
        return true;
    }
}
