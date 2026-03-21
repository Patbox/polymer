package eu.pb4.polymer.core.mixin.client.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
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
public abstract class CreativeModeTabsMixin {
    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(method = "streamAllTabs", at = @At("RETURN"),require = 0)
    private static Stream<CreativeModeTab> polymerCore$injectClientItemGroups(Stream<CreativeModeTab> original) {
        if (InternalClientRegistry.CREATIVE_TAB.size() > 0) {
            return Stream.concat(original, InternalClientRegistry.CREATIVE_TAB.stream());
        }
        return original;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "lambda$bootstrap$14", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;acceptAll(Ljava/util/Collection;)V", shift = At.Shift.BEFORE),  require = 0)
    private static void polymerCore$injectClientSearch(Registry registry, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output search, CallbackInfo ci, @Local Set<ItemStack> set) {
        for (var group : InternalClientRegistry.CREATIVE_TAB) {
            set.addAll(group.getSearchTabDisplayItems());
        }
    }
}
