package dev.waystonescustomizations.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.waystonescustomizations.icon.GroupIcons;
import dev.waystonescustomizations.icon.ItemIcons;
import net.blay09.mods.waystones.client.WaystoneGroupIcons;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/** Adds our pixel-art icons, the item icons and the pictures from the icons folder to the icons Waystones offers. */
@Mixin(WaystoneGroupIcons.class)
public abstract class WaystoneGroupIconsMixin {
    @ModifyReturnValue(method = "getPresetIcons", at = @At("RETURN"))
    private static List<Identifier> waystonescustomizations$addIcons(List<Identifier> original) {
        GroupIcons.rememberWaystonesIcons(original);
        // Waystones' own icons stay first, so new groups keep Waystones' default icon
        List<Identifier> result = new ArrayList<>(original);
        result.addAll(GroupIcons.allBuiltin());
        result.addAll(ItemIcons.all());
        result.addAll(GroupIcons.customIcons());
        return result;
    }
}
