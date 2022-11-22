package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.impl.interfaces.TempPlayerLoginAttachments;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;<init>(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)V", shift = At.Shift.AFTER))
    private void polymer$setupHandler(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        var handshake = ((TempPlayerLoginAttachments) player).polymer$getAndRemoveHandshakeHandler();

        if (handshake != null) {
            handshake.apply(player.networkHandler);
            PolymerServerProtocol.sendSyncPackets(player.networkHandler, false);
        }

        var packets = ((TempPlayerLoginAttachments) player).polymer$getLatePackets();
        if (packets != null) {
            ((TempPlayerLoginAttachments) player).polymer$setLatePackets(null);
            for (var packet : packets) {
                try {
                    packet.apply(player.networkHandler);
                } catch (Throwable e) {

                }
            }
        }

        if (((TempPlayerLoginAttachments) player).polymer$getForceRespawnPacket()) {
            var world = player.getWorld();
            connection.send(new PlayerRespawnS2CPacket(world.getDimensionKey(), world.getRegistryKey(), BiomeAccess.hashSeed(((ServerWorld) world).getSeed()),player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), world.isDebugWorld(), ((ServerWorld) world).isFlat(), false, player.getLastDeathPos()));
        }
    }
}
