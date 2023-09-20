package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.api.util.ServerDynamicPacket;
import eu.pb4.polymer.networking.impl.*;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin implements NetworkHandlerExtension {
    @Shadow @Final
    ClientConnection connection;

    @Shadow @Nullable private GameProfile profile;

    @Shadow public abstract void onEnterConfiguration(EnterConfigurationC2SPacket packet);

    @Shadow @Final
    MinecraftServer server;
    @Unique
    private boolean polymerNet$ignoreCall = false;

    @Nullable
    @Unique
    private SyncedClientOptions polymerNet$overrideOptions;

    @Override
    public long polymerNet$lastPacketUpdate(Identifier packet) {
        return 0;
    }

    @Override
    public void polymerNet$savePacketTime(Identifier packet) {
    }

    @Override
    public ClientConnection polymerNet$getConnection() {
        return this.connection;
    }

    @Inject(method = "onEnterConfiguration", at = @At("HEAD"), cancellable = true)
    private void polymerNet$prePlayHandshakeHackfest(EnterConfigurationC2SPacket packet, CallbackInfo ci) {
        if (this.polymerNet$ignoreCall) {
            return;
        }
        ci.cancel();
        EarlyConfigurationConnectionMagic.handle(this.profile, SyncedClientOptions.createDefault(), (ServerLoginNetworkHandler) (Object) this, this.server, connection, (context) -> {
            connection.disableAutoRead();
            var attr = ((ExtClientConnection) connection).polymerNet$getChannel().attr(ClientConnection.SERVERBOUND_PROTOCOL_KEY);
            attr.set(NetworkState.LOGIN.getHandler(NetworkSide.SERVERBOUND));
            connection.setPacketListener((ServerLoginNetworkHandler) (Object) this);
            attr.set(NetworkState.CONFIGURATION.getHandler(NetworkSide.SERVERBOUND));

            if (connection.isOpen()) {
                this.polymerNet$ignoreCall = true;
                this.polymerNet$overrideOptions = context.options().getValue();
                this.onEnterConfiguration(packet);
                if (this.connection.getPacketListener() instanceof ServerConfigurationPacketListener listener) {
                    for (var packetx : context.storedPackets()) {
                        try {
                            packetx.apply(listener);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
                this.connection.enableAutoRead();
            }
        });
    }

    @ModifyArg(method = "onEnterConfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationNetworkHandler;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ConnectedClientData;)V"))
    private ConnectedClientData polymerNet$swapClientData(ConnectedClientData clientData) {
        if (this.polymerNet$overrideOptions != null) {
            return new ConnectedClientData(clientData.gameProfile(), clientData.latency(), this.polymerNet$overrideOptions);
        }
        return clientData;
    }
}
