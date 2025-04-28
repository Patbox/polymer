package eu.pb4.polymer.blocks.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.blocks.api.PolymerSoundBlock;
import eu.pb4.polymer.blocks.impl.PolymerBlockSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// actually send step sounds, not just locally or for everyone but the player in case of the Player class
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract World getWorld();

    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();

    @Shadow public abstract SoundCategory getSoundCategory();

    @WrapOperation(method = "playCombinationStepSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void filament$combinationStepSounds(Entity instance, SoundEvent soundEvent, float f, float g, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState) {
        if ((Object)this instanceof ServerPlayerEntity && blockState.getBlock() instanceof PolymerSoundBlock || PolymerBlockSounds.REMIXES.containsValue(blockState.getSoundGroup()))
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundCategory(), f, g);
        else
            original.call(instance, soundEvent, f, g);
    }

    @WrapOperation(method = "playSecondaryStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void filament$muffledStepSounds(Entity instance, SoundEvent soundEvent, float f, float g, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState) {
        if ((Object)this instanceof ServerPlayerEntity && blockState.getBlock() instanceof PolymerSoundBlock || PolymerBlockSounds.REMIXES.containsValue(blockState.getSoundGroup()))
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundCategory(), f, g);
        else
            original.call(instance, soundEvent, f, g);
    }

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void filament$stepSounds(Entity instance, SoundEvent soundEvent, float f, float g, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) BlockState blockState) {
        if ((Object)this instanceof ServerPlayerEntity && blockState.getBlock() instanceof PolymerSoundBlock || PolymerBlockSounds.REMIXES.containsValue(blockState.getSoundGroup()))
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundCategory(), f, g);
        else
            original.call(instance, soundEvent, f, g);
    }
}
