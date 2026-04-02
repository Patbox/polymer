package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
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

import java.util.Set;
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 1500)
public class CreativeModeTabsMixin {
    @Environment(EnvType.SERVER)
    @ModifyReturnValue(method = "streamAllTabs", at = @At("RETURN"))
    private static Stream<CreativeModeTab> polymerCore$injectServerItemGroups(Stream<CreativeModeTab> original) {
        if (PolymerCreativeModeTabUtils.REGISTRY.size() > 0) {
            return Stream.concat(original, PolymerCreativeModeTabUtils.REGISTRY.stream());
        }
        return original;
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "lambda$bootstrap$14", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;acceptAll(Ljava/util/Collection;)V", shift = At.Shift.BEFORE), require = 0)
    private static void polymerCore$injectServerSearch(Registry<CreativeModeTab> registry, CreativeModeTab.ItemDisplayParameters displayContext, CreativeModeTab.Output entries, CallbackInfo ci, @Local Set<ItemStack> set) {
        for (var group : PolymerCreativeModeTabUtils.REGISTRY) {
            set.addAll(group.getSearchTabDisplayItems());
        }
    }

    @Environment(EnvType.CLIENT)
    @ModifyExpressionValue(method = "buildAllTabContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;streamAllTabs()Ljava/util/stream/Stream;"))
    private static Stream<CreativeModeTab> polymerCore$injectServerItemGroupsForReset(Stream<CreativeModeTab> original) {
        if (PolymerCreativeModeTabUtils.REGISTRY.size() > 0) {
            return Stream.concat(original, PolymerCreativeModeTabUtils.REGISTRY.stream());
        }
        return original;
    }

    @Inject(method = "buildAllTabContents", at = @At("HEAD"))
    private static void polymerCore$clearCache(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
        PolymerCreativeModeTabUtils.invalidateCache();
    }
}
