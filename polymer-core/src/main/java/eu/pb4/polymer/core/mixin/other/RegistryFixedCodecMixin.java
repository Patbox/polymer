package eu.pb4.polymer.core.mixin.other;

import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RegistryFixedCodec.class)
public class RegistryFixedCodecMixin {
    @Shadow @Final private RegistryKey<Registry> registry;

    @ModifyVariable(
            method = "encode(Lnet/minecraft/registry/entry/RegistryEntry;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            at = @At("HEAD")
    )
    private RegistryEntry<?> polymerCore$swapEntry(RegistryEntry<?> entry) {

        if (PolymerCommonUtils.isNetworkingThread()) {
            var player = PolymerUtils.getPlayerContext();
            try {
                if (entry.value() instanceof PolymerSyncedObject polymerSyncedObject) {
                    return ((Registry<Registry>) (Object) Registries.REGISTRIES).get(this.registry).getEntry(polymerSyncedObject.getPolymerReplacement(player));
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return entry;
    }
}
