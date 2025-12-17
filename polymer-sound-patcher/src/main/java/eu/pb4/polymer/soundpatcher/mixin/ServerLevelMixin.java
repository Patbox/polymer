package eu.pb4.polymer.soundpatcher.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @ModifyVariable(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException(Entity entity, @Local(argsOnly = true) Holder<SoundEvent> soundEvent) {
        return SoundRemapperImpl.ignoreExceptions(soundEvent.value()) ? null : entity;
    }

    @ModifyVariable(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Entity ignoreEntityException2(Entity entity, @Local(argsOnly = true) Holder<SoundEvent> soundEvent) {
        return SoundRemapperImpl.ignoreExceptions(soundEvent.value()) ? null : entity;
    }
}
