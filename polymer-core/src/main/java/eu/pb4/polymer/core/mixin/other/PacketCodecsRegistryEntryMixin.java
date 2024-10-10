package eu.pb4.polymer.core.mixin.other;


import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$19", priority = 500)
public abstract class PacketCodecsRegistryEntryMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntry;)V", at = @At("HEAD"), argsOnly = true)
    private RegistryEntry<?> polymer$changeData(RegistryEntry<?> val, RegistryByteBuf buf) {
        var player = PacketContext.get();

        if (val.value() instanceof SoundEvent soundEvent && RegistrySyncUtils.isServerEntry(Registries.SOUND_EVENT, soundEvent)) {
            return RegistryEntry.of(val.value());
        }

        /*if (val.value() instanceof PolymerSoundEvent syncedObject) {
            var replacement = syncedObject.getPolymerReplacement(player);

            if (replacement instanceof PolymerSoundEvent) {
                return RegistryEntry.of(replacement);
            }


            return Registries.SOUND_EVENT.getEntry(replacement);
        }*/


        return val;
    }

}