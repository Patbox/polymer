package eu.pb4.polymer.core.mixin.client.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(value = ItemGroups.class, priority = 1500)
public abstract class ItemGroupsMixin {
    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(method = "stream", at = @At("RETURN"),require = 0)
    private static Stream<ItemGroup> polymerCore$injectClientItemGroups(Stream<ItemGroup> original) {
        if (InternalClientRegistry.ITEM_GROUPS.size() > 0) {
            return Stream.concat(original, InternalClientRegistry.ITEM_GROUPS.stream());
        }
        return original;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "method_51316", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup$Entries;addAll(Ljava/util/Collection;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private static void polymerCore$injectClientSearch(Registry<ItemGroup> registry, ItemGroup.DisplayContext displayContext, ItemGroup.Entries entries, CallbackInfo ci, Set<ItemStack> set) {
        for (var group : InternalClientRegistry.ITEM_GROUPS) {
            set.addAll(group.getSearchTabStacks());
        }
    }
}
