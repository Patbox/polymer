package eu.pb4.polymer.core.mixin.client.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
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
    @Environment(EnvType.CLIENT)
    @Inject(method = "stream", at = @At("RETURN"), cancellable = true)
    private static void polymerCore$injectClientItemGroups(CallbackInfoReturnable<Stream<ItemGroup>> cir) {
        if (InternalClientRegistry.ITEM_GROUPS.size() > 0) {
            cir.setReturnValue(Stream.concat(cir.getReturnValue(), InternalClientRegistry.ITEM_GROUPS.stream()));
        }
    }
}
