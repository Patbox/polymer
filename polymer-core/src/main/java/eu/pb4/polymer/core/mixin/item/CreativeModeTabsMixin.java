package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 1500)
public class CreativeModeTabsMixin {
    @Environment(EnvType.SERVER)
    @ModifyReturnValue(method = "streamAllTabs", at = @At("RETURN"))
    private static Stream<CreativeModeTab> polymerCore$injectServerItemGroups(Stream<CreativeModeTab> original) {
        if (PolymerItemGroupUtils.REGISTRY.size() > 0) {
            return Stream.concat(original, PolymerItemGroupUtils.REGISTRY.stream());
        }
        return original;
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "lambda$bootstrap$14", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;acceptAll(Ljava/util/Collection;)V", shift = At.Shift.BEFORE), require = 0)
    private static void polymerCore$injectServerSearch(Registry<CreativeModeTab> registry, CreativeModeTab.ItemDisplayParameters displayContext, CreativeModeTab.Output entries, CallbackInfo ci, @Local Set<ItemStack> set) {
        for (var group : PolymerItemGroupUtils.REGISTRY) {
            set.addAll(group.getSearchTabDisplayItems());
        }
    }
}
