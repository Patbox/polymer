package eu.pb4.polymer.blocks.mixin.sound;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// enable step sounds
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerMixin extends PlayerEntity {
    public PlayerMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Inject(method = "increaseTravelMotionStats", at = @At("HEAD"))
    public void filament$checkMovementStatistics(double d, double e, double f, CallbackInfo ci) {
        // run step sound checks etc
        if (!hasVehicle() && d != 0 && e != 0 && f != 0) ((EntityInvoker)this).invokeApplyMovementEmissionAndPlaySound(MoveEffect.SOUNDS, new Vec3d(d,e,f), this.getSteppingPos(), this.getSteppingBlockState());
    }
}
