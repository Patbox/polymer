package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

@Mixin(RegistryElementCodec.class)
public class RegistryElementCodecMixin {

    @Shadow @Final private RegistryKey<Registry> registryRef;

    @ModifyVariable(
            method = "encode(Lnet/minecraft/registry/entry/RegistryEntry;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            at = @At("HEAD")
    )
    private RegistryEntry<?> polymerCore$swapEntry(RegistryEntry<?> entry, @Local(argsOnly = true) DynamicOps<?> ops) {
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            var player = PacketContext.get();
            try {
                //noinspection unchecked,rawtypes
                var registry = ((Registry<Registry>) (Object) Registries.REGISTRIES).get(this.registryRef);
                //noinspection unchecked

                if (PolymerSyncedObject.getSyncedObject(registry, entry.value()) instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                    var obj = ((PolymerSyncedObject<Object>) polymerSyncedObject).getPolymerReplacement(entry.value(), player);
                    if (obj == null) {
                        obj = entry.value();
                    }

                    //noinspection unchecked,DataFlowIssue
                    var x = registry.getEntry(obj);
                    //noinspection unchecked,DataFlowIssue
                    var key = (Optional<RegistryKey<?>>) x.getKey();
                    if (key.isPresent() && !key.get().getValue().getNamespace().equals("minecraft")) {
                        return RegistryEntry.of(obj);
                    }

                    return x;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return entry;
    }
}
