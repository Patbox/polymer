package eu.pb4.polymer.blocks.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.blocks.api.PolymerSoundBlock;
import eu.pb4.polymer.blocks.impl.PolymerBlockSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// play fall sounds serverside
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "playBlockFallSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void polymer$stepSounds(LivingEntity instance, SoundEvent soundEvent, float vol, float pitch, Operation<Void> original, @Local BlockState blockState) {
        if ((Object)this instanceof ServerPlayerEntity && blockState.getBlock() instanceof PolymerSoundBlock || PolymerBlockSounds.REMIXES.containsValue(blockState.getSoundGroup()))
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundCategory(), vol, pitch);
        else
            original.call(instance, soundEvent, vol, pitch);
    }
}
