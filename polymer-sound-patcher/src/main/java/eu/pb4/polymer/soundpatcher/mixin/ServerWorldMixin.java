package eu.pb4.polymer.soundpatcher.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @ModifyVariable(method = "playSound", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException(Entity entity, @Local(argsOnly = true) RegistryEntry<SoundEvent> soundEvent) {
        return SoundRemapperImpl.ignoreExceptions(soundEvent.value()) ? null : entity;
    }

    @ModifyVariable(method = "playSoundFromEntity", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException2(Entity entity, @Local(argsOnly = true) RegistryEntry<SoundEvent> soundEvent) {
        return SoundRemapperImpl.ignoreExceptions(soundEvent.value()) ? null : entity;
    }
}
