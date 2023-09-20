package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.api.util.ServerDynamicPacket;
import eu.pb4.polymer.networking.impl.NetworkHandlerExtension;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements NetworkHandlerExtension {
    @Unique
    private final Object2LongMap<Identifier> polymerNet$rateLimits = new Object2LongOpenHashMap<>();

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow @Final protected ClientConnection connection;

    @Shadow @Final protected MinecraftServer server;

    @Override
    public long polymerNet$lastPacketUpdate(Identifier packet) {
        return this.polymerNet$rateLimits.getLong(packet);
    }

    @Override
    public void polymerNet$savePacketTime(Identifier packet) {
        this.polymerNet$rateLimits.put(packet, System.currentTimeMillis());
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (ServerPacketRegistry.handle(this.server, (ServerCommonNetworkHandler) (Object) this, packet.payload())) {
            this.polymerNet$savePacketTime(packet.payload().id());
            ci.cancel();
        }
    }

    @ModifyVariable(method = "send", at = @At("HEAD"))
    private Packet<?> polymerNet$replacePacket(Packet<?> packet) {
        if (packet instanceof ServerDynamicPacket dynamicPacket) {
            var out = dynamicPacket.createPacket((ServerCommonNetworkHandler) (Object) (this), ((Object) this) instanceof ServerPlayNetworkHandler h ? h.getPlayer() : null);

            if (out != null) {
                return out;
            }
        }

        return packet;
    }

    @Override
    public ClientConnection polymerNet$getConnection() {
        return this.connection;
    }

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void polymerNet$dontLeakDynamic(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (packet instanceof ServerDynamicPacket) {
            ci.cancel();
        }
    }
}
