package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow
    @Final
    public MinecraftServer server;
    @Shadow
    public ServerPlayNetworkHandler networkHandler;


    @Inject(method = "sendUnloadChunkPacket", at = @At("HEAD"))
    private void polymerVE$chunkUnload(ChunkPos chunkPos, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null && new ChunkPos(BlockPos.ofFloored(holder.getPos())).equals(chunkPos)) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void polymerVE$removeFromHologramsOnDisconnect(CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            holder.stopWatching(this.networkHandler);
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void polymerVE$removeOnDeath(DamageSource source, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }

    @Inject(method = "moveToWorld", at = @At("HEAD"))
    private void polymerVE$removeOnWorldChange(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At(value = "RETURN", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getWorld()Lnet/minecraft/server/world/ServerWorld;"))
    private void polymerVE$removeOnWorldChange2(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null) {
                holder.getAttachment().updateTracking(this.networkHandler);
            }
        }
    }
}
