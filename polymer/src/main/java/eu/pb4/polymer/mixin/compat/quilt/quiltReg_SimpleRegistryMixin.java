package eu.pb4.polymer.mixin.compat.quilt;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.impl.compat.QuiltRegistryUtils;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleRegistry.class)
public class quiltReg_SimpleRegistryMixin<T> {
    @Inject(method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/util/registry/RegistryEntry;", at = @At("TAIL"))
    private <V extends T> void polymer_markQuiltSync(int rawId, RegistryKey<T> key, T entry, Lifecycle lifecycle, boolean checkDuplicateKeys, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        QuiltRegistryUtils.markAsOptional((SimpleRegistry<?>) (Object) this, key.getValue());
    }
}
