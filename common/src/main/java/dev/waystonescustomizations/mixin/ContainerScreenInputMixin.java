package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.ScrollTarget;
import dev.waystonescustomizations.gui.popup.Popups;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives open popups all input, and passes the mouse wheel on to our widgets. */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenInputMixin extends Screen {
    private ContainerScreenInputMixin(Component title) {
        super(title);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$click(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.click(this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$release(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.isOpen(this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$drag(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.isOpen(this)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$key(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.key(this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$scroll(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (Popups.scroll(this, mouseX, mouseY, scrollY)) {
            cir.setReturnValue(true);
            return;
        }
        for (GuiEventListener child : children()) {
            if (child instanceof ScrollTarget target && target.waystonescustomizations$scroll(mouseX, mouseY, scrollY)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
