package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {
    @WrapWithCondition(method = "sendToNearbyPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;sendToSelfAndListeners(Lnet/minecraft/network/packet/Packet;)V"))
    private boolean wrapSendToNearbyForMoreControl(ServerChunkLoadingManager.EntityTracker instance, Packet<?> packet, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            //noinspection unchecked
            polymerEntity.onEntityPacketSent((Consumer<Packet<?>>) (Object) ((Consumer<Packet<ClientPlayPacketListener>>) instance::sendToSelfAndListeners), packet);
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "sendToOtherNearbyPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;sendToListeners(Lnet/minecraft/network/packet/Packet;)V"))
    private boolean wrapSendToOtherNearbyForMoreControl(ServerChunkLoadingManager.EntityTracker instance, Packet<?> packet, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            //noinspection unchecked
            polymerEntity.onEntityPacketSent((Consumer<Packet<?>>) (Object) ((Consumer<Packet<ClientPlayPacketListener>>) instance::sendToListeners), packet);
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "sendToOtherNearbyPlayersIf", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;sendToListenersIf(Lnet/minecraft/network/packet/Packet;Ljava/util/function/Predicate;)V"))
    private boolean wrapSendToOtherNearbyForMoreControl(ServerChunkLoadingManager.EntityTracker instance, Packet<? super ClientPlayPacketListener> packet, Predicate<ServerPlayerEntity> predicate, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            //noinspection unchecked
            polymerEntity.onEntityPacketSent(x -> instance.sendToListenersIf((Packet<? super ClientPlayPacketListener>) x, predicate), packet);
            return false;
        }
        return true;
    }
}
