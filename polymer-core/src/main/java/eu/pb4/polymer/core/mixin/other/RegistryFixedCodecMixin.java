package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
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
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

@Mixin(RegistryFixedCodec.class)
public class RegistryFixedCodecMixin {
    @Shadow @Final private RegistryKey<Registry> registry;

    @ModifyVariable(
            method = "encode(Lnet/minecraft/registry/entry/RegistryEntry;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            at = @At("HEAD")
    )
    private RegistryEntry<?> polymerCore$swapEntry(RegistryEntry<?> entry) {
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            var player = PacketContext.get();
            try {
                if (entry.value() instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                    var registry = ((Registry<Registry>) (Object) Registries.REGISTRIES).get(this.registry);
                    var x = registry.getEntry(polymerSyncedObject.getPolymerReplacement(player));
                    if (x == null) {
                        return (RegistryEntry<?>) registry.getEntry(0).orElse(entry);
                    }
                    return x;
                } else if (entry.value() instanceof EntityType<?> type && PolymerEntityUtils.isPolymerEntityType(type)) {
                    return EntityType.MARKER.getRegistryEntry();
                } else if (entry.value() instanceof EntityAttribute && PolymerEntityUtils.isPolymerEntityAttribute((RegistryEntry<EntityAttribute>) entry)) {
                    return EntityAttributes.SPAWN_REINFORCEMENTS;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return entry;
    }
}
