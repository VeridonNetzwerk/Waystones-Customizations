package dev.waystonescustomizations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.waystonescustomizations.icon.GroupIcons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/** Every place where Waystones draws a group icon: draw item icons and pictures from the icons folder ourselves. */
@Pseudo
@Mixin(targets = {
        "net.blay09.mods.waystones.client.gui.widget.WaystoneGroupButton",
        "net.blay09.mods.waystones.client.gui.widget.WaystoneGroupFilterButton",
        "net.blay09.mods.waystones.client.gui.widget.ManageWaystoneGroupButton",
        "net.blay09.mods.waystones.client.gui.widget.WaystoneGroupIconButton",
})
public abstract class GroupIconRenderMixin {
    @WrapOperation(method = "extractContents", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void waystonescustomizations$drawIcon(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier sprite, int x, int y, int w, int h,
                                         Operation<Void> original) {
        if (GroupIcons.needsCustomDraw(sprite)) {
            GroupIcons.drawCustom(g, sprite, x, y, w, h);
        } else {
            original.call(g, pipeline, sprite, x, y, w, h);
        }
    }
}
