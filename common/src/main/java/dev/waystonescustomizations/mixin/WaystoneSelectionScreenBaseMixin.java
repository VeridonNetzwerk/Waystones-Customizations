package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.ThemeButton;
import dev.waystonescustomizations.gui.ThemedScreen;
import dev.waystonescustomizations.gui.popup.Popups;
import net.blay09.mods.waystones.client.gui.screen.WaystoneContainerScreen;
import net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreenBase;
import net.blay09.mods.waystones.menu.WaystoneSelectionMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the theme button next to the waystone list. */
@Mixin(WaystoneSelectionScreenBase.class)
public abstract class WaystoneSelectionScreenBaseMixin extends WaystoneContainerScreen<WaystoneSelectionMenu> {
    private ThemeButton waystonescustomizations$themeButton;

    private WaystoneSelectionScreenBaseMixin(WaystoneSelectionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 0, 0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void waystonescustomizations$addThemeButton(CallbackInfo ci) {
        waystonescustomizations$themeButton = null;
        Popups.close();
        int[] b = ((ThemedScreen) this).waystonescustomizations$panelBounds();
        int x = b[0] + b[2] + 3;
        int y = b[1];
        if (x + ThemeButton.SIZE > width) {
            x = b[0] + b[2] - ThemeButton.SIZE - 4;
            y = b[1] + 4;
        }
        waystonescustomizations$themeButton = addRenderableWidget(new ThemeButton(this, x, y));
    }
}
