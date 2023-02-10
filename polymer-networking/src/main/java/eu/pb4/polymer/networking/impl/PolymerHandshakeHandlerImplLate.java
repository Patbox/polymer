package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerHandshakeHandlerImplLate implements PolymerHandshakeHandler {
    private final MinecraftServer server;
    private final ServerPlayNetworkHandler handler;
    private final NetworkHandlerExtension polymerHandler;

    public PolymerHandshakeHandlerImplLate(MinecraftServer server, ServerPlayNetworkHandler handler) {
        this.server = server;
        this.handler = handler;
        this.polymerHandler = NetworkHandlerExtension.of(handler);

        //PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c -> c.accept(this)));
    }

    public void sendPacket(Packet<?> packet) {
        this.handler.sendPacket(packet);
    }

    public void set(String polymerVersion, Object2IntMap<Identifier> protocolVersions) {
        this.polymerHandler.polymerNet$setVersion(polymerVersion);
        for (var entry : protocolVersions.object2IntEntrySet()) {
            this.polymerHandler.polymerNet$setSupportedVersion(entry.getKey(), entry.getIntValue());
        }
    }

    public boolean isPolymer() {
        return this.polymerHandler.polymerNet$hasPolymer();
    }

    public String getPolymerVersion() {
        return this.polymerHandler.polymerNet$version();
    }

    public int getSupportedProtocol(Identifier identifier) {
        return this.polymerHandler.polymerNet$getSupportedVersion(identifier);
    }

    @Override
    public void setLastPacketTime(Identifier identifier) {
        this.polymerHandler.polymerNet$savePacketTime(identifier);
    }

    @Override
    public long getLastPacketTime(Identifier identifier) {
        return this.polymerHandler.polymerNet$lastPacketUpdate(identifier);

    }

    public MinecraftServer getServer() {
        return server;
    }

    public ServerPlayerEntity getPlayer() {
        return this.handler.getPlayer();
    }

    public static PolymerHandshakeHandler of(ServerPlayNetworkHandler handler) {
        return new PolymerHandshakeHandlerImplLate(handler.getPlayer().getServer(), handler);
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        // No need to apply, as it applies by default!
        PolymerServerNetworking.AFTER_HANDSHAKE_APPLY.invoke(x -> x.accept(handler, this));
    }

    @Override
    public boolean getPackStatus() {
        return ((CommonResourcePackInfoHolder) this.polymerHandler).polymerCommon$hasResourcePack();
    }

    @Override
    public void reset() {
        this.polymerHandler.polymerNet$resetSupported();
    }

    @Override
    public void setPackStatus(boolean status) {
        ((CommonResourcePackInfoHolder) this.polymerHandler).polymerCommon$setResourcePack(status);
    }
}