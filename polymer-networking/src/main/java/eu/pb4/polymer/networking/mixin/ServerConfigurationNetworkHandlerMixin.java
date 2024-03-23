package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.impl.EarlyPlayConnectionMagic;
import eu.pb4.polymer.networking.impl.ExtClientConnection;
import eu.pb4.polymer.networking.impl.NetImpl;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
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
        if (NetImpl.IS_DISABLED || true) {
            original.call(manager, connection, player, clientData);
            return;
        }

        EarlyPlayConnectionMagic.handle(player, clientData.syncedOptions(), (ServerConfigurationNetworkHandler) (Object) this, player.server, connection, (context) -> {
            ((ExtClientConnection) connection).polymerNet$wrongPacketConsumer(context.storedPackets()::add);

            if (connection.isOpen()) {
                var oldPlayer = player.server.getPlayerManager().getPlayer(this.getProfile().getId());
                if (oldPlayer != null) {
                    this.disconnect(Text.translatable("multiplayer.disconnect.duplicate_login"));
                } else {
                    //original.call(manager, connection, player, new ConnectedClientData(clientData.gameProfile(), clientData.latency(), context.options().getValue()));
                    ((ExtClientConnection) connection).polymerNet$wrongPacketConsumer(null);
                    if (this.connection.getPacketListener() instanceof ServerPlayPacketListener listener) {
                        for (var packetx : context.storedPackets()) {
                            try {
                                packetx.apply(listener);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //connection.enableAutoRead();
                }
            }
        });
    }
}
