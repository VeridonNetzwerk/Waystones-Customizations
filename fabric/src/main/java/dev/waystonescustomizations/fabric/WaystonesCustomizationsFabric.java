package dev.waystonescustomizations.fabric;

import dev.waystonescustomizations.WaystonesCustomizations;
import net.fabricmc.api.ClientModInitializer;

public class WaystonesCustomizationsFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WaystonesCustomizations.init();
    }
}
