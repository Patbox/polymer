package eu.pb4.polymer.soundpatcher.mixin;


import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/network/codec/PacketCodecs$22", priority = 1200)
public abstract class PacketCodecsRegistryEntryMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/registry/entry/RegistryEntry;)V", at = @At("HEAD"), argsOnly = true)
    private RegistryEntry<?> polymer$changeData(RegistryEntry<?> val, RegistryByteBuf buf) {
        if (val.value() instanceof SoundEvent soundEvent) {
            var x = SoundRemapperImpl.remap(soundEvent);

            if (x != soundEvent) {
                return RegistryEntry.of(x);
            }
        }

        return val;
    }

}