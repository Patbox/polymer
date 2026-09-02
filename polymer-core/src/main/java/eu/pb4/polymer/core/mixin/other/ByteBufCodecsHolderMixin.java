package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$33", priority = 500)
public abstract class ByteBufCodecsHolderMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/core/Holder;)V", at = @At("HEAD"), argsOnly = true)
    private Holder<?> polymer$changeData(Holder<?> val, RegistryFriendlyByteBuf buf) {
        var player = PacketContext.get();

        if (val.value() instanceof SoundEvent soundEvent) {
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.SOUND_EVENT, soundEvent) instanceof PolymerSoundEvent syncedObject) {
                var replacement = syncedObject.getPolymerReplacement(soundEvent, player);

                if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.SOUND_EVENT, replacement) instanceof PolymerSoundEvent) {
                    return Holder.direct(replacement);
                }

                return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(replacement);
            } else if (RegistrySyncUtils.isServerEntry(BuiltInRegistries.SOUND_EVENT, soundEvent)) {
                return Holder.direct(soundEvent);
            }
        }

        return val;
    }

}