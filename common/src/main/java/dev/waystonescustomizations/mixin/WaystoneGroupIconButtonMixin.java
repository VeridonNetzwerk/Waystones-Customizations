package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.IconButtonAccess;
import dev.waystonescustomizations.icon.GroupIcons;
import net.blay09.mods.waystones.client.gui.widget.WaystoneGroupIconButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaystoneGroupIconButton.class)
public abstract class WaystoneGroupIconButtonMixin extends Button implements IconButtonAccess {
    @Shadow
    private Identifier icon;

    @Shadow
    private void updateTooltip() {
        throw new AssertionError();
    }

    private WaystoneGroupIconButtonMixin(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration narration) {
        super(x, y, width, height, message, onPress, narration);
    }

    @Override
    public Identifier waystonescustomizations$getIcon() {
        return icon;
    }

    @Override
    public void waystonescustomizations$setIcon(Identifier newIcon) {
        icon = newIcon;
        updateTooltip();
    }

    /** Waystones looks up a translation that often does not exist; use a real name for every icon. */
    @Inject(method = "updateTooltip", at = @At("HEAD"), cancellable = true)
    private void waystonescustomizations$iconTooltip(CallbackInfo ci) {
        if (icon != null) {
            setTooltip(Tooltip.create(Component.translatable("gui.waystones.group_settings.icon", GroupIcons.displayName(icon))));
            ci.cancel();
        }
    }
}
