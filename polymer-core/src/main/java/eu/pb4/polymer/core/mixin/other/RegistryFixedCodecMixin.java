package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.codec.RegistryFixedCodec;
import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

@Mixin(RegistryFixedCodec.class)
public class RegistryFixedCodecMixin {
    @Shadow @Final private ResourceKey<Registry> registryKey;

    @ModifyVariable(
            method = "encode(Lnet/minecraft/core/Holder;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
            at = @At("HEAD")
    )
    private Holder<?> polymerCore$swapEntry(Holder<?> entry, @Local(argsOnly = true) DynamicOps<?> ops) {
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            var player = PacketContext.get();
            try {
                //noinspection unchecked,rawtypes
                var registry = ((Registry<Registry>) (Object) BuiltInRegistries.REGISTRY).getValue(this.registryKey);
                //noinspection unchecked
                if (entry.value() instanceof EntityType<?> type && PolymerEntityUtils.isPolymerEntityType(type)) {
                    return EntityTypes.MARKER.builtInRegistryHolder();
                } else if (entry.value() instanceof Attribute && PolymerEntityUtils.isPolymerAttribute((Holder<Attribute>) entry)) {
                    return Attributes.SPAWN_REINFORCEMENTS_CHANCE;
                } else if (PolymerSyncedObject.getSyncedObject(registry, entry.value()) instanceof PolymerSyncedObject<?> polymerSyncedObject) {
                    //noinspection unchecked,DataFlowIssue
                    var x = registry.wrapAsHolder(((PolymerSyncedObject<Object>) polymerSyncedObject).getPolymerReplacement(entry.value(), player));
                    if (x == null) {
                        //noinspection unchecked
                        return (Holder<?>) registry.get(0).orElse(entry);
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
