package eu.pb4.polymer.soundpatcher.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.soundpatcher.api.SoundPatcher;
import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(Entity.class)
public class EntityMixin {
    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void playSoundCorrectlyForBlocks(Entity instance, SoundEvent sound, float volume, float pitch, Operation<Void> original, @Local(argsOnly = true) BlockState state) {
        if (instance instanceof ServerPlayer player
                && SoundRemapperImpl.SOUND_EXCEPTION_IGNORER.contains(CoreBridge.getClientSideSoundGroup(state, PacketContext.create(player)).getStepSound().location())) {
            try (var t = SoundRemapperImpl.ignorePlaySoundExclusion()) {
                original.call(instance, sound, volume, pitch);
            }
        } else {
            original.call(instance, sound, volume, pitch);
        }
    }
}
