package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/DifficultyS2CPacket;<init>(Lnet/minecraft/world/Difficulty;Z)V", shift = At.Shift.AFTER))
    private void polymerNet$setupHandler(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        var handshake = ((TempPlayerLoginAttachments) player).polymerNet$getAndRemoveHandshakeHandler();

        if (handshake != null) {
            handshake.apply(player.networkHandler);

        }
        PolymerServerNetworking.ON_PLAY_SYNC.invoke(x -> x.accept(player.networkHandler, handshake));

        if (((TempPlayerLoginAttachments) player).polymerNet$getForceRespawnPacket()) {
            var world = player.getServerWorld();
            connection.send(new PlayerRespawnS2CPacket(player.createCommonPlayerSpawnInfo(world), PlayerRespawnS2CPacket.KEEP_ALL));
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    private void polymerNet$storePlayer(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        if (player.server.isHost(player.getGameProfile())) {
            ClientUtils.backupPlayer = player;
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void polymerNet$removePlayer(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        if (player.server.isHost(player.getGameProfile())) {
            ClientUtils.backupPlayer = null;
        }
    }
}
