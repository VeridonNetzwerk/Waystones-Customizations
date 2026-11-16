package dev.waystonescustomizations.gui;

import dev.waystonescustomizations.ThemeConfig;
import dev.waystonescustomizations.gui.popup.Popups;
import dev.waystonescustomizations.gui.popup.ThemeListPopup;
import dev.waystonescustomizations.theme.Theme;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Small button in the waystone menu that opens the theme list. */
public class ThemeButton extends Button {
    public static final int SIZE = 20;

    public ThemeButton(Screen screen, int x, int y) {
        super(x, y, SIZE, SIZE, Component.translatable("waystonescustomizations.button.theme"), button -> open(screen, button), DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable("waystonescustomizations.button.theme")
                .append("\n")
                .append(Component.translatable("waystonescustomizations.button.theme.hint").withStyle(ChatFormatting.GRAY))));
    }

    private static void open(Screen screen, Button button) {
        Popups.open(screen, new ThemeListPopup(button.getX(), button.getRight(), button.getY(), screen.width, screen.height));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        extractDefaultSprite(g);
        Theme theme = ThemeConfig.get().activeTheme();
        g.fakeItem(new ItemStack(BuiltInRegistries.ITEM.getValue(theme.icon())), getX() + 2, getY() + 2);
    }
}
