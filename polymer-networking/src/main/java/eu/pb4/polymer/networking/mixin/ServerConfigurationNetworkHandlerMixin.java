package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.impl.EarlyPlayConnectionMagic;
import eu.pb4.polymer.networking.impl.ExtClientConnection;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerConfigurationNetworkHandler.class, priority = 900)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonNetworkHandler {
    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }
    @Shadow
    protected abstract GameProfile getProfile();

    @WrapOperation(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V"))
    private void polymerNet$prePlayHandshakeHackfest(PlayerManager manager, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, Operation<Void> original) {
        EarlyPlayConnectionMagic.handle(player, clientData.syncedOptions(), (ServerConfigurationNetworkHandler) (Object) this, player.server, connection, (context) -> {
            ((ExtClientConnection) connection).polymerNet$ignorePacketsUntilChange(context.storedPackets()::add);
            connection.disableAutoRead();
            var attr = ((ExtClientConnection) connection).polymerNet$getChannel().attr(ClientConnection.SERVERBOUND_PROTOCOL_KEY);
            attr.set(NetworkState.CONFIGURATION.getHandler(NetworkSide.SERVERBOUND));
            connection.setPacketListener(this);
            attr.set(NetworkState.PLAY.getHandler(NetworkSide.SERVERBOUND));

            if (connection.isOpen()) {
                var oldPlayer = player.server.getPlayerManager().getPlayer(this.getProfile().getId());
                if (oldPlayer != null) {
                    this.disconnect(Text.translatable("multiplayer.disconnect.duplicate_login"));
                } else {
                    original.call(manager, connection, player, new ConnectedClientData(clientData.gameProfile(), clientData.latency(), context.options().getValue()));
                    connection.enableAutoRead();
                }
            }
        });
    }
}
