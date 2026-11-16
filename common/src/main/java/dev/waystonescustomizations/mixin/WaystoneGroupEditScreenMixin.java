package dev.waystonescustomizations.mixin;

import dev.waystonescustomizations.gui.IconButtonAccess;
import dev.waystonescustomizations.gui.IconPickerWidget;
import dev.waystonescustomizations.gui.popup.BackgroundPopup;
import dev.waystonescustomizations.gui.popup.Popups;
import net.blay09.mods.waystones.api.WaystoneGroup;
import net.blay09.mods.waystones.client.gui.widget.ColorButton;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import net.blay09.mods.waystones.client.gui.screen.WaystoneContainerScreen;
import net.blay09.mods.waystones.client.gui.screen.WaystoneGroupEditScreen;
import net.blay09.mods.waystones.client.gui.widget.WaystoneGroupIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/** Shows a grid with all group icons next to the group editor, instead of clicking through them one by one. */
@Mixin(WaystoneGroupEditScreen.class)
public abstract class WaystoneGroupEditScreenMixin extends WaystoneContainerScreen<AbstractContainerMenu> {
    private static final int GAP = 6;

    @Shadow
    private WaystoneGroupIconButton iconButton;

    @Shadow
    private ColorButton colorButton;

    @Shadow
    @Final
    private Identifier groupId;

    private WaystoneGroupEditScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 0, 0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void waystonescustomizations$addIconPicker(CallbackInfo ci) {
        if (iconButton == null) {
            return;
        }
        IconButtonAccess access = (IconButtonAccess) iconButton;

        int spaceRight = width - (leftPos + imageWidth + GAP) - 4;
        int spaceLeft = leftPos - GAP - 4;
        int space = Math.max(spaceRight, spaceLeft);
        int columns = Math.clamp((space - IconPickerWidget.widthFor(0)) / IconPickerWidget.CELL, 4, 9);
        int pickerWidth = IconPickerWidget.widthFor(columns);
        int maxRows = (height - 8 - IconPickerWidget.heightFor(columns, 0)) / IconPickerWidget.CELL;
        int rows = Math.clamp(maxRows, 2, 7);
        int pickerHeight = IconPickerWidget.heightFor(columns, rows);

        int x = spaceRight >= pickerWidth || spaceRight >= spaceLeft ? leftPos + imageWidth + GAP : leftPos - GAP - pickerWidth;
        int y = Math.clamp(topPos + (imageHeight - pickerHeight) / 2, 4, Math.max(4, height - pickerHeight - 4));

        addRenderableWidget(new IconPickerWidget(x, y, columns, rows,
                access::waystonescustomizations$getIcon, access::waystonescustomizations$setIcon));
        waystonescustomizations$addStyleButton();
    }

    /** Opens the entry background / text colour picker for this group (only for saved groups). */
    private void waystonescustomizations$addStyleButton() {
        if (colorButton == null) {
            return;
        }
        int bx = colorButton.getRight() + 4;
        int bw = leftPos + imageWidth - 8 - bx;
        if (bw < 40) {
            return;
        }
        WaystoneGroup group = waystonescustomizations$findGroup();
        Button button = Button.builder(Component.translatable("waystonescustomizations.group.entry_style"), b -> {
            WaystoneGroup current = waystonescustomizations$findGroup();
            if (current != null) {
                Popups.open(this, BackgroundPopup.forGroup(current, b.getX(), b.getBottom() + 34, width, height));
            }
        }).bounds(bx, colorButton.getY(), bw, colorButton.getHeight()).build();
        if (group == null) {
            button.active = false;
            button.setTooltip(Tooltip.create(Component.translatable("waystonescustomizations.group.save_first")));
        }
        addRenderableWidget(button);
    }

    private @Nullable WaystoneGroup waystonescustomizations$findGroup() {
        var player = Minecraft.getInstance().player;
        if (player == null || groupId == null) {
            return null;
        }
        for (WaystoneGroup group : PlayerWaystoneManager.getWaystoneGroupRegistry(player)) {
            if (group.identifier().equals(groupId)) {
                return group;
            }
        }
        return null;
    }
}
