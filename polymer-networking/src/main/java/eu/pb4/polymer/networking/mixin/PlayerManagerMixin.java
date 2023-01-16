package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
    private void polymerNet$setupHandler(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        var handshake = ((TempPlayerLoginAttachments) player).polymerNet$getAndRemoveHandshakeHandler();

        if (handshake != null) {
            handshake.apply(player.networkHandler);

            PolymerServerNetworking.AFTER_HANDSHAKE_APPLY.invoke(x -> x.accept(player.networkHandler, handshake));
        }

        var packets = ((TempPlayerLoginAttachments) player).polymerNet$getLatePackets();
        if (packets != null) {
            ((TempPlayerLoginAttachments) player).polymerNet$setLatePackets(null);
            for (var packet : packets) {
                try {
                    packet.apply(player.networkHandler);
                } catch (Throwable e) {

                }
            }
        }

        if (((TempPlayerLoginAttachments) player).polymerNet$getForceRespawnPacket()) {
            var world = player.getWorld();
            connection.send(new PlayerRespawnS2CPacket(world.getDimensionKey(), world.getRegistryKey(), BiomeAccess.hashSeed(((ServerWorld) world).getSeed()),player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), world.isDebugWorld(), ((ServerWorld) world).isFlat(), (byte) 0xFF, player.getLastDeathPos()));
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void polymerNet$storePlayer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (player.server.isHost(player.getGameProfile())) {
            ClientUtils.backupPlayer = player;
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void polymerNet$removePlayer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (player.server.isHost(player.getGameProfile())) {
            ClientUtils.backupPlayer = null;
        }
    }
}
