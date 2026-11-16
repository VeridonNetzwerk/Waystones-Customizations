package dev.waystonescustomizations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.gui.ThemedScreen;
import dev.waystonescustomizations.gui.popup.Popup;
import dev.waystonescustomizations.gui.popup.Popups;
import dev.waystonescustomizations.gui.EntryClicks;
import dev.waystonescustomizations.theme.Theme;
import dev.waystonescustomizations.theme.ThemeRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    /** Draws the theme panel right after the normal (blurred/darkened) screen background. */
    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void waystonescustomizations$drawPanel(GuiGraphicsExtractor g, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!((Object) this instanceof ThemedScreen screen)) {
            return;
        }
        Theme theme = ThemeConfig.get().activeTheme();
        if (theme.isVanilla()) {
            return;
        }
        int[] b = screen.waystonescustomizations$panelBounds();
        ThemeRenderer.drawPanel(g, b[0], b[1], b[2], b[3], theme);
        if (b.length > 4) {
            ThemeRenderer.drawHeaderBand(g, b[0] + 3, b[1] + 3, b[2] - 6, b[4] - 2 - (b[1] + 3), theme);
        }
    }

    /**
     * While a popup is open the screen below is drawn as if the mouse was far away (no hover effects,
     * no tooltips), then the popup is drawn on its own layer on top.
     */
    /** A popup belongs to its screen: when the screen goes away (closed, replaced), the popup goes too. */
    @Inject(method = "removed", at = @At("HEAD"))
    private void waystonescustomizations$closePopup(CallbackInfo ci) {
        if (Popups.on(this) != null) {
            Popups.close();
        }
        EntryClicks.forget();
    }

    @WrapOperation(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    private void waystonescustomizations$drawPopup(Screen self, GuiGraphicsExtractor g, int mouseX, int mouseY, float a, Operation<Void> original) {
        Popup popup = Popups.on(self);
        if (popup == null) {
            original.call(self, g, mouseX, mouseY, a);
            return;
        }
        original.call(self, g, -10000, -10000, a);
        g.nextStratum();
        popup.render(g, mouseX, mouseY, a);
    }
}
