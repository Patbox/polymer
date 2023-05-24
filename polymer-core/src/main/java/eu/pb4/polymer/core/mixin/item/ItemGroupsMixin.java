package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {
    @Environment(EnvType.SERVER)
    @Inject(method = "stream", at = @At("RETURN"), cancellable = true)
    private static void polymerCore$injectServerItemGroups(CallbackInfoReturnable<Stream<ItemGroup>> cir) {
        if (PolymerItemGroupUtils.REGISTRY.size() > 0) {
            cir.setReturnValue(Stream.concat(cir.getReturnValue(), PolymerItemGroupUtils.REGISTRY.stream()));
        }
    }
}
