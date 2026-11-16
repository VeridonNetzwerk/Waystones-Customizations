package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.popup.Popups;
import net.blay09.mods.waystones.client.gui.screen.WaystoneGroupEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The group editor handles clicks and keys itself first, so popups are checked here. */
@Mixin(WaystoneGroupEditScreen.class)
public abstract class GroupEditScreenInputMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$click(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.click(this, event)) {
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
