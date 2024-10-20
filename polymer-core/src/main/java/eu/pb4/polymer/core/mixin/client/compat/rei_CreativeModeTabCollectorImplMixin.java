package eu.pb4.polymer.core.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.client.InternalClientItemGroup;
import me.shedaniel.rei.impl.client.fabric.CreativeModeTabCollectorImpl;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(CreativeModeTabCollectorImpl.class)
public class rei_CreativeModeTabCollectorImplMixin {
    @WrapOperation(method = "collectTabs", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseThrow(Ljava/util/function/Supplier;)Ljava/lang/Object;"), remap = false)
    private static Object getItemGroupKey(Optional instance, Supplier<Object> exceptionSupplier, Operation<Object> original, @Local ItemGroup group) {
        if (instance.isEmpty() && group instanceof InternalClientItemGroup group1) {
            return group1.getKey();
        }
        return original.call(instance, exceptionSupplier);
    }
}
