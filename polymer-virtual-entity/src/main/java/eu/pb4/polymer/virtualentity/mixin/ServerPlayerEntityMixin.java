package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow
    @Final
    public MinecraftServer server;
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void polymerVE$removeFromHologramsOnDisconnect(CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            holder.stopWatching(this.networkHandler);
        }
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void polymerVE$removeOnDeath(DamageSource source, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFZ)Z", at = @At(value = "RETURN"))
    private void polymerVE$removeOnWorldChange(ServerWorld serverWorld, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/player/PlayerEntity;", at = @At(value = "RETURN"))
    private void polymerVE$removeOnWorldChange3(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }

    @Inject(method = "refreshPositionAfterTeleport", at = @At(value = "RETURN"))
    private void polymerVE$removeOnWorldChange2(double x, double y, double z, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }


}
