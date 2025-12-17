package eu.pb4.polymer.core.mixin.client.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 1500)
public abstract class CreativeModeTabsMixin {
    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(method = "streamAllTabs", at = @At("RETURN"),require = 0)
    private static Stream<CreativeModeTab> polymerCore$injectClientItemGroups(Stream<CreativeModeTab> original) {
        if (InternalClientRegistry.ITEM_GROUPS.size() > 0) {
            return Stream.concat(original, InternalClientRegistry.ITEM_GROUPS.stream());
        }
        return original;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "method_51316", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;acceptAll(Ljava/util/Collection;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private static void polymerCore$injectClientSearch(Registry<CreativeModeTab> registry, CreativeModeTab.ItemDisplayParameters displayContext, CreativeModeTab.Output entries, CallbackInfo ci, Set<ItemStack> set) {
        for (var group : InternalClientRegistry.ITEM_GROUPS) {
            set.addAll(group.getSearchTabDisplayItems());
        }
    }
}
