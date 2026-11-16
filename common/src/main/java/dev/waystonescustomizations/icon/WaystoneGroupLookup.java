package dev.waystonescustomizations.icon;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.api.WaystoneGroup;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** Finds the (visible) groups a waystone belongs to, in the player's group order. */
public final class WaystoneGroupLookup {
    private WaystoneGroupLookup() {
    }

    public static List<WaystoneGroup> groupsOf(Waystone waystone) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return List.of();
        }
        Set<Identifier> ids = waystone.getWaystoneGroups();
        if (ids.isEmpty()) {
            return List.of();
        }
        Collection<WaystoneGroup> registry = PlayerWaystoneManager.getWaystoneGroupRegistry(player);
        List<WaystoneGroup> result = new ArrayList<>();
        for (WaystoneGroup group : registry) {
            if (group.hidden() || !ids.contains(group.identifier())) {
                continue;
            }
            // Every waystone is in its dimension group automatically; Waystones already shows the dimension.
            if (group.identifier().getPath().startsWith("dimension/")) {
                continue;
            }
            result.add(group);
        }
        return result;
    }
}
