package eu.pb4.polymer.core.mixin.client.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.core.impl.client.ClientDebugFlags;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @WrapOperation(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object replaceIdentifier(ItemStack instance, DataComponentType componentType, Operation<Object> original) {
        if (ClientDebugFlags.customItemModels) return original.call(instance, componentType);
        return instance.getItem().components().get(componentType);
    }
}
