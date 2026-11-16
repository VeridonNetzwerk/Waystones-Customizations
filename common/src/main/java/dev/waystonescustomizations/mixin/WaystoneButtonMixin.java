package dev.waystonescustomizations.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.gui.EntryClicks;
import dev.waystonescustomizations.icon.GroupIcons;
import dev.waystonescustomizations.icon.WaystoneGroupLookup;
import dev.waystonescustomizations.style.EntryBackground;
import dev.waystonescustomizations.style.EntryStyles;
import dev.waystonescustomizations.theme.Theme;
import dev.waystonescustomizations.theme.ThemeRenderer;
import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneGroup;
import net.blay09.mods.waystones.client.WaystoneGroupIcons;
import net.blay09.mods.waystones.client.gui.widget.AbstractWaystoneButton;
import net.blay09.mods.waystones.client.gui.widget.WaystoneButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/** Entry background (theme, group colour or the player's choice), group icons and the brush button. */
@Mixin(WaystoneButton.class)
public abstract class WaystoneButtonMixin extends AbstractWaystoneButton {
    private static final int MAX_ICONS = 3;
    private static final int ICON_SIZE = 16;
    private static final int ICON_STEP = 17;

    private WaystoneButtonMixin(int x, int y, int width, Waystone waystone, OnPress onPress) {
        super(x, y, width, waystone, onPress);
    }

    private List<WaystoneGroup> waystonescustomizations$groups() {
        return WaystoneGroupLookup.groupsOf(waystone);
    }

    @WrapOperation(method = "extractContents", at = @At(value = "INVOKE",
            target = "Lnet/blay09/mods/waystones/client/gui/widget/WaystoneButton;extractDefaultSprite(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    private void waystonescustomizations$drawEntry(WaystoneButton self, GuiGraphicsExtractor g, Operation<Void> original) {
        ThemeConfig config = ThemeConfig.get();
        Theme theme = config.activeTheme();
        List<WaystoneGroup> groups = waystonescustomizations$groups();
        int groupColor = groups.isEmpty() ? 0 : groups.getFirst().color() & 0xFFFFFF;
        int bar = config.showGroupColorBar ? groupColor : 0;
        boolean hovered = isHoveredOrFocused();
        String background = EntryStyles.get().resolve(waystone, groups);

        if (EntryBackground.overridesTheme(background, groupColor)) {
            EntryBackground.fill(g, background, groupColor, getX(), getY(), getWidth(), getHeight(), hovered && active);
            ThemeRenderer.drawEntryFrame(g, getX(), getY(), getWidth(), getHeight(), hovered, active,
                    theme.isVanilla() ? 0xFFFFFFFF : theme.accent(), bar);
            return;
        }
        if (theme.isVanilla()) {
            original.call(self, g);
            if (bar != 0) {
                g.fill(getX() + 2, getY() + 2, getX() + 5, getY() + getHeight() - 2, 0xFF000000 | bar);
            }
            return;
        }
        ThemeRenderer.drawEntry(g, getX(), getY(), getWidth(), getHeight(), hovered, active, theme, bar);
    }

    /** While hovered, a small brush left of the distance / cost opens the background picker. */
    @WrapOperation(method = "extractContents", at = @At(value = "INVOKE",
            target = "Lnet/blay09/mods/waystones/client/gui/widget/WaystoneButton;renderRightContent(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)I"))
    private int waystonescustomizations$drawBrush(WaystoneButton self, GuiGraphicsExtractor g, Operation<Integer> original,
                                         @Local(argsOnly = true, ordinal = 0) int mouseX,
                                         @Local(argsOnly = true, ordinal = 1) int mouseY) {
        int used = original.call(self, g);
        if (!isHovered()) {
            return used;
        }
        int size = EntryClicks.BRUSH_SIZE;
        int bx = getX() + getWidth() - 2 - used - size - 2;
        int by = getY() + (getHeight() - size) / 2;
        EntryClicks.track(this, waystone, bx, by);
        boolean overBrush = EntryClicks.isOverBrush(mouseX, mouseY);
        if (overBrush) {
            g.fill(bx - 1, by - 1, bx + size + 1, by + size + 1, 0x60FFFFFF);
            g.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("waystonescustomizations.brush.tooltip"), mouseX, mouseY);
        }
        g.blitSprite(RenderPipelines.GUI_TEXTURED, GroupIcons.sprite("brush"), bx, by, size, size);
        return used + size + 4;
    }

    @WrapOperation(method = "extractContents", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/ActiveTextCollector;acceptScrolling(Lnet/minecraft/network/chat/Component;IIIII)V"))
    private void waystonescustomizations$drawGroupIcons(ActiveTextCollector output, Component message, int centerX, int left, int right, int top, int bottom,
                                               Operation<Void> original,
                                               @Local(argsOnly = true) GuiGraphicsExtractor g,
                                               @Local(argsOnly = true, ordinal = 0) int mouseX,
                                               @Local(argsOnly = true, ordinal = 1) int mouseY) {
        Integer textColor = EntryStyles.get().resolveText(waystone, waystonescustomizations$groups());
        if (textColor != null) {
            message = Component.literal(message.getString()).withColor(textColor);
        }
        if (!ThemeConfig.get().showGroupIcons) {
            original.call(output, message, centerX, left, right, top, bottom);
            return;
        }
        List<WaystoneGroup> groups = waystonescustomizations$groups();
        int count = Math.min(groups.size(), MAX_ICONS);
        if (count == 0) {
            original.call(output, message, centerX, left, right, top, bottom);
            return;
        }
        int x = Math.max(left, getX() + 6);
        int y = getY() + (getHeight() - ICON_SIZE) / 2;
        for (int i = 0; i < count; i++) {
            WaystoneGroup group = groups.get(i);
            int ix = x + i * ICON_STEP;
            GroupIcons.draw(g, WaystoneGroupIcons.getIcon(group), ix, y, ICON_SIZE, ICON_SIZE);
            if (mouseX >= ix && mouseX < ix + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE) {
                g.setTooltipForNextFrame(Minecraft.getInstance().font, group.name(), mouseX, mouseY);
            }
        }
        if (groups.size() > MAX_ICONS) {
            g.text(Minecraft.getInstance().font, "+", x + count * ICON_STEP - 2, y + 8, 0xFFFFFFFF, true);
        }
        int used = x + count * ICON_STEP + (groups.size() > MAX_ICONS ? 6 : 1) - left;
        original.call(output, message, centerX + used / 2, left + used, right, top, bottom);
    }
}
