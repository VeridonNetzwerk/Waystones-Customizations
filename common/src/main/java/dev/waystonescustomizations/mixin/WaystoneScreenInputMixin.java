package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.EntryClicks;
import dev.waystonescustomizations.gui.popup.Popups;
import net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreenBase;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Waystones' waystone list screen handles some input themselves before Minecraft sees it, so popups and the
 * entry right click are checked here first.
 */
@Mixin(WaystoneSelectionScreenBase.class)
public abstract class WaystoneScreenInputMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$click(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.click(this, event)) {
            cir.setReturnValue(true);
            return;
        }
        if (EntryClicks.click((Screen) (Object) this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$drag(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.isOpen(this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$scroll(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.scroll(this, mouseX, mouseY, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$key(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.key(this, event)) {
            cir.setReturnValue(true);
        }
    }
}
