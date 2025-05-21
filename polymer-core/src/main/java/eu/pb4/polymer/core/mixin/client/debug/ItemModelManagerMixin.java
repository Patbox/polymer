package eu.pb4.polymer.core.mixin.client.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.core.impl.client.ClientDebugFlags;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
    private Object replaceIdentifier(ItemStack instance, ComponentType componentType, Operation<Object> original) {
        if (ClientDebugFlags.customItemModels) return original.call(instance, componentType);
        return instance.getItem().getComponents().get(componentType);
    }
}
