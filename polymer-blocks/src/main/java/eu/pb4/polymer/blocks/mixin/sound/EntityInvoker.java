package eu.pb4.polymer.blocks.mixin.sound;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker(value = "applyMoveEffect")
    void invokeApplyMovementEmissionAndPlaySound(Entity.MoveEffect moveEffect, Vec3d vec3, BlockPos blockPos, BlockState blockState);
}
