package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements PacketListenerImplExtension {
    @Unique
    private final Object2LongMap<Identifier> polymerNet$rateLimits = new Object2LongOpenHashMap<>();
    @Shadow
    @Final
    protected Connection connection;
    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    public abstract void send(Packet<?> packet);

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

    @Override
    public Connection polymerNet$getConnection() {
        return this.connection;
    }

    @Override
    public @Nullable RegistryAccess polymer$getDynamicRegistryManager() {
        return this.server.registryAccess();
    }
}
