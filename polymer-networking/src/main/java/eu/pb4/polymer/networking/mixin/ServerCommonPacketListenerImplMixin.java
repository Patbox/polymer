package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.api.util.ServerDynamicPacket;
import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import io.netty.channel.ChannelFutureListener;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements PacketListenerImplExtension {
    @Unique
    private final Object2LongMap<Identifier> polymerNet$rateLimits = new Object2LongOpenHashMap<>();

    @Shadow
    public abstract void send(Packet<?> packet);

    @Shadow @Final protected Connection connection;

    @Shadow @Final protected MinecraftServer server;

    @Override
    public long polymerNet$lastPacketUpdate(Identifier packet) {
        return this.polymerNet$rateLimits.getLong(packet);
    }

    @Override
    public void polymerNet$savePacketTime(Identifier packet) {
        this.polymerNet$rateLimits.put(packet, System.currentTimeMillis());
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (ServerPacketRegistry.handle(this.server, (ServerCommonPacketListenerImpl) (Object) this, packet.payload())) {
            this.polymerNet$savePacketTime(packet.payload().type().id());
            ci.cancel();
        }
    }

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"))
    private Packet<?> polymerNet$replacePacket(Packet<?> packet) {
        if (packet instanceof ServerDynamicPacket dynamicPacket) {
            var out = dynamicPacket.createPacket((ServerCommonPacketListenerImpl) (Object) (this), ((Object) this) instanceof ServerGamePacketListenerImpl h ? h.getPlayer() : null);

            if (out != null) {
                return out;
            }
        }

        return packet;
    }

    @Override
    public Connection polymerNet$getConnection() {
        return this.connection;
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void polymerNet$dontLeakDynamic(Packet<?> packet, ChannelFutureListener listener, CallbackInfo ci) {
        if (packet instanceof ServerDynamicPacket) {
            ci.cancel();
        }
    }

    @Override
    public @Nullable RegistryAccess polymer$getDynamicRegistryManager() {
        return this.server.registryAccess();
    }
}
