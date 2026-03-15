package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundChangeDifficultyPacket;<init>(Lnet/minecraft/world/Difficulty;Z)V", shift = At.Shift.AFTER))
    private void polymerNet$setupHandler(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        var handshake = ((TempPlayerLoginAttachments) player).polymerNet$getAndRemoveHandshakeHandler();

        if (handshake != null) {
            handshake.apply(player.connection);

        }
        PolymerServerNetworking.ON_PLAY_SYNC.invoker().accept(player.connection, handshake);

        if (((TempPlayerLoginAttachments) player).polymerNet$getForceRespawnPacket()) {
            var world = player.level();
            connection.send(new ClientboundRespawnPacket(player.createCommonSpawnInfo(world), ClientboundRespawnPacket.KEEP_ALL_DATA));
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void polymerNet$storePlayer(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        if (player.level().getServer().isSingleplayerOwner(player.nameAndId())) {
            ClientUtils.backupPlayer = player;
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void polymerNet$removePlayer(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        if (player.level().getServer().isSingleplayerOwner(player.nameAndId())) {
            ClientUtils.backupPlayer = null;
        }
    }
}
