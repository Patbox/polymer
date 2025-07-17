package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {
    @WrapWithCondition(method = "sendToNearbyPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;sendToNearbyPlayers(Lnet/minecraft/network/packet/Packet;)V"))
    private boolean wrapSendToNearbyForMoreControl(ServerChunkLoadingManager.EntityTracker instance, Packet<?> packet, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            polymerEntity.onEntityPacketSent(instance::sendToNearbyPlayers, packet);
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "sendToOtherNearbyPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;sendToOtherNearbyPlayers(Lnet/minecraft/network/packet/Packet;)V"))
    private boolean wrapSendToOtherNearbyForMoreControl(ServerChunkLoadingManager.EntityTracker instance, Packet<?> packet, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            polymerEntity.onEntityPacketSent(instance::sendToOtherNearbyPlayers, packet);
            return false;
        }
        return true;
    }
}
