package eu.pb4.polymer.soundpatcher.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(method = "playBlockFallSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void playSoundCorrectlyForBlocks(LivingEntity instance, SoundEvent sound, float volume, float pitch, Operation<Void> original, @Local BlockState state) {
        if (instance instanceof ServerPlayerEntity player
                && SoundRemapperImpl.SOUND_EXCEPTION_IGNORER.contains(CoreBridge.getClientSideSoundGroup(state, PacketContext.create(player)).getStepSound().id())) {
            try (var t = SoundPatcher.ignorePlaySoundExclusion()) {
                original.call(instance, sound, volume, pitch);
            }
        } else {
            original.call(instance, sound, volume, pitch);
        }
    }
}
