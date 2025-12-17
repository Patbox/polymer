package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.impl.*;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements PacketListenerImplExtension {
    @Shadow @Final
    Connection connection;

    @Shadow @Nullable private GameProfile authenticatedProfile;

    @Shadow public abstract void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket packet);

    @Shadow @Final
    MinecraftServer server;

    @Shadow public abstract void onDisconnect(DisconnectionDetails info);

    @Unique
    private boolean polymerNet$ignoreCall = false;

    @Nullable
    @Unique
    private AtomicReference<ClientInformation> polymerNet$overrideOptions;

    @Override
    public long polymerNet$lastPacketUpdate(Identifier packet) {
        return 0;
    }

    @Override
    public void polymerNet$savePacketTime(Identifier packet) {
    }

    @Override
    public Connection polymerNet$getConnection() {
        return this.connection;
    }

    @WrapWithCondition(method = "handleLoginAcknowledgement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
    private boolean dontDuplicateCalls(Connection instance, ProtocolInfo<?> newState) {
        return NetImpl.IS_DISABLED;
    }

    @WrapOperation(method = "handleLoginAcknowledgement", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void dontDuplicateCalls2(Connection instance, ProtocolInfo<PacketListener> state, PacketListener packetListener, Operation<Void> original) {
        if (NetImpl.IS_DISABLED) {
            original.call(instance, state, packetListener);
        } else {
            ((ConnectionAccessor) instance).setPacketListener(packetListener);
        }
    }

    @Inject(method = "handleLoginAcknowledgement", at = @At("HEAD"), cancellable = true)
    private void polymerNet$prePlayHandshakeHackfest(ServerboundLoginAcknowledgedPacket packet, CallbackInfo ci) {
        if (this.polymerNet$ignoreCall || NetImpl.IS_DISABLED) {
            return;
        }
        ci.cancel();
        this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
        var defaultOptions = ClientInformation.createDefault();
        EarlyConfigurationConnectionMagic.handle(this.authenticatedProfile, defaultOptions, (ServerLoginPacketListenerImpl) (Object) this, this.server, connection, (context) -> {
            ((ExtConnection) connection).polymerNet$wrongPacketConsumer(context.storedPackets()::add);

            if (connection.isConnected()) {
                this.polymerNet$ignoreCall = true;
                if (context.options().get() != defaultOptions) {
                    this.polymerNet$overrideOptions = context.options();
                }
                this.handleLoginAcknowledgement(packet);
                ((ExtConnection) connection).polymerNet$wrongPacketConsumer(null);
                //this.connection.enableAutoRead();
                if (this.connection.getPacketListener() instanceof ServerConfigurationPacketListener listener) {
                    for (var packetx : context.storedPackets()) {
                        try {
                            //noinspection unchecked
                            ((Packet<ServerConfigurationPacketListener>) packetx).handle(listener);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @ModifyArg(method = "handleLoginAcknowledgement", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/CommonListenerCookie;)V"))
    private CommonListenerCookie polymerNet$swapClientData(CommonListenerCookie clientData) {
        if (this.polymerNet$overrideOptions != null) {
            return new CommonListenerCookie(clientData.gameProfile(), clientData.latency(), this.polymerNet$overrideOptions.get(), clientData.transferred());
        }
        return clientData;
    }
}
