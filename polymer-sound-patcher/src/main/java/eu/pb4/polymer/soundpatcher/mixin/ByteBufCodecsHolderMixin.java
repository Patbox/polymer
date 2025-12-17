package eu.pb4.polymer.soundpatcher.mixin;


import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$30", priority = 1200)
public abstract class ByteBufCodecsHolderMixin {
    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/core/Holder;)V", at = @At("HEAD"), argsOnly = true)
    private Holder<?> polymer$changeData(Holder<?> val, RegistryFriendlyByteBuf buf) {
        if (val.value() instanceof SoundEvent soundEvent) {
            var x = SoundRemapperImpl.remap(soundEvent);

            if (x != soundEvent) {
                return Holder.direct(x);
            }
        }

        return val;
    }

}