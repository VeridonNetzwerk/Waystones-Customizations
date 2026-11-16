package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.theme.ThemeRenderer;
import net.blay09.mods.waystones.client.gui.widget.AbstractWaystoneList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sunken "well" behind the waystone list. */
@Mixin(AbstractWaystoneList.class)
public abstract class AbstractWaystoneListMixin {
    @Inject(method = "extractListBackground", at = @At("HEAD"))
    private void waystonescustomizations$drawWell(GuiGraphicsExtractor g, CallbackInfo ci) {
        if (ThemeConfig.get().activeTheme().isVanilla()) {
            return;
        }
        AbstractWidget self = (AbstractWidget) (Object) this;
        ThemeRenderer.drawWell(g, self.getX() - 2, self.getY() - 2, self.getWidth() + 4, self.getHeight() + 4);
    }
}
