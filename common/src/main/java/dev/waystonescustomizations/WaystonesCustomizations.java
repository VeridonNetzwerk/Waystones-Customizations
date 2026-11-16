package dev.waystonescustomizations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared entry point, called by the Fabric and NeoForge loader classes. */
public final class WaystonesCustomizations {
    public static final String MOD_ID = "waystonescustomizations";
    public static final Logger LOGGER = LoggerFactory.getLogger("Waystones Customizations");

    private WaystonesCustomizations() {
    }

    public static void init() {
        LOGGER.info("Waystones Customizations loaded");
    }

    /** Dev only: forces every hooked Waystones class to load so broken mixins show up. */
    public static boolean smokeTest() {
        String[] targets = {
                "net.blay09.mods.waystones.client.gui.screen.WaystoneSelectionScreenBase",
                "net.blay09.mods.waystones.client.gui.screen.WaystoneGroupEditScreen",
                "net.blay09.mods.waystones.client.gui.widget.AbstractWaystoneList",
                "net.blay09.mods.waystones.client.gui.widget.WaystoneButton",
                "net.blay09.mods.waystones.client.gui.widget.WaystoneGroupButton",
                "net.blay09.mods.waystones.client.gui.widget.WaystoneGroupFilterButton",
                "net.blay09.mods.waystones.client.gui.widget.ManageWaystoneGroupButton",
                "net.blay09.mods.waystones.client.gui.widget.WaystoneGroupIconButton",
                "net.blay09.mods.waystones.client.WaystoneGroupIcons",
        };
        boolean ok = true;
        for (String target : targets) {
            try {
                Class.forName(target, true, WaystonesCustomizations.class.getClassLoader());
            } catch (Throwable t) {
                ok = false;
                LOGGER.error("SMOKETEST could not load {}", target, t);
            }
        }
        try {
            int icons = net.blay09.mods.waystones.client.WaystoneGroupIcons.getPresetIcons().size();
            LOGGER.info("SMOKETEST preset icons: {}", icons);
            ok &= icons > 100;
        } catch (Throwable t) {
            ok = false;
            LOGGER.error("SMOKETEST getPresetIcons failed", t);
        }
        return ok;
    }
}
