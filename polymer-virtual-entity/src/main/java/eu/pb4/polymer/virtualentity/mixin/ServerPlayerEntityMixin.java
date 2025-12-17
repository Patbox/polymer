package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Set;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @Shadow
    @Final
    public MinecraftServer server;
    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void polymerVE$removeFromHologramsOnDisconnect(CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.connection).polymer$getHolders())) {
            holder.stopWatching(this.connection);
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void polymerVE$removeOnDeath(DamageSource source, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.connection).polymer$getHolders())) {
            var att = holder.getAttachment();
            if (att != null) {
                att.updateTracking(this.connection);
            }
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At(value = "RETURN"))
    private void polymerVE$removeOnWorldChange(ServerLevel serverWorld, double destX, double destY, double destZ, Set<Relative> flags, float yaw, float pitch, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        for (var holder : new ArrayList<>(((HolderHolder) this.connection).polymer$getHolders())) {
            var att = holder.getAttachment();
            if (att != null) {
                att.updateTracking(this.connection);
            }
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "RETURN"))
    private void polymerVE$removeOnWorldChange3(TeleportTransition teleportTarget, CallbackInfoReturnable<Entity> cir) {
        for (var holder : new ArrayList<>(((HolderHolder) this.connection).polymer$getHolders())) {
            var att = holder.getAttachment();
            if (att != null) {
                att.updateTracking(this.connection);
            }
        }
    }

    @Inject(method = "snapTo", at = @At(value = "RETURN"))
    private void polymerVE$removeOnWorldChange2(double x, double y, double z, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) this.connection).polymer$getHolders())) {
            var att = holder.getAttachment();
            if (att != null) {
                att.updateTracking(this.connection);
            }
        }
    }


}
