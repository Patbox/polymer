package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(ItemGroups.class)
public class ItemGroupsMixin {
    @Environment(EnvType.SERVER)
    @Inject(method = "stream", at = @At("RETURN"), cancellable = true, require = 0)
    private static void polymerCore$injectServerItemGroups(CallbackInfoReturnable<Stream<ItemGroup>> cir) {
        if (PolymerItemGroupUtils.REGISTRY.size() > 0) {
            cir.setReturnValue(Stream.concat(cir.getReturnValue(), PolymerItemGroupUtils.REGISTRY.stream()));
        }
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "method_51316", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup$Entries;addAll(Ljava/util/Collection;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private static void polymerCore$injectServerSearch(Registry<ItemGroup> registry, ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries, CallbackInfo ci, Set<ItemStack> set) {
        for (var group : PolymerItemGroupUtils.REGISTRY) {
            set.addAll(group.getSearchTabStacks());
        }
    }
}
