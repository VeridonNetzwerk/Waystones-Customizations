package dev.waystonescustomizations.neoforge;

import dev.waystonescustomizations.WaystonesCustomizations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = WaystonesCustomizations.MOD_ID, dist = Dist.CLIENT)
public class WaystonesCustomizationsNeoForge {
    public WaystonesCustomizationsNeoForge() {
        WaystonesCustomizations.init();
        if (Boolean.getBoolean("waystonescustomizations.smoketest")) {
            NeoForge.EVENT_BUS.addListener(WaystonesCustomizationsNeoForge::smokeTest);
        }
    }

    /** Dev only: loads every class we hook into (so all mixins must apply) and quits. */
    private static void smokeTest(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }
        boolean ok = WaystonesCustomizations.smokeTest();
        WaystonesCustomizations.LOGGER.info("SMOKETEST RESULT: {}", ok ? "PASS" : "FAIL");
        Minecraft.getInstance().stop();
    }
}
