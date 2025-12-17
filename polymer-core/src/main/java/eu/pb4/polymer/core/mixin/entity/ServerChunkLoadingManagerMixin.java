package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(ChunkMap.class)
public class ServerChunkLoadingManagerMixin {
    @WrapWithCondition(method = "sendToTrackingPlayersAndSelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap$TrackedEntity;sendToTrackingPlayersAndSelf(Lnet/minecraft/network/protocol/Packet;)V"))
    private boolean wrapSendToNearbyForMoreControl(ChunkMap.TrackedEntity instance, Packet<?> packet, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            //noinspection unchecked
            polymerEntity.onEntityPacketSent((Consumer<Packet<?>>) (Object) ((Consumer<Packet<ClientGamePacketListener>>) instance::sendToTrackingPlayersAndSelf), packet);
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "sendToTrackingPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap$TrackedEntity;sendToTrackingPlayers(Lnet/minecraft/network/protocol/Packet;)V"))
    private boolean wrapSendToOtherNearbyForMoreControl(ChunkMap.TrackedEntity instance, Packet<?> packet, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            //noinspection unchecked
            polymerEntity.onEntityPacketSent((Consumer<Packet<?>>) (Object) ((Consumer<Packet<ClientGamePacketListener>>) instance::sendToTrackingPlayers), packet);
            return false;
        }
        return true;
    }

    @WrapWithCondition(method = "sendToTrackingPlayersFiltered", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap$TrackedEntity;sendToTrackingPlayersFiltered(Lnet/minecraft/network/protocol/Packet;Ljava/util/function/Predicate;)V"))
    private boolean wrapSendToOtherNearbyForMoreControl(ChunkMap.TrackedEntity instance, Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate, @Local(argsOnly = true) Entity entity) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null) {
            //noinspection unchecked
            polymerEntity.onEntityPacketSent(x -> instance.sendToTrackingPlayersFiltered((Packet<? super ClientGamePacketListener>) x, predicate), packet);
            return false;
        }
        return true;
    }
}
