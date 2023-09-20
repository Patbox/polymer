package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonClientConnectionExt;
import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerHandshakeHandlerImplLate implements PolymerHandshakeHandler {
    private final MinecraftServer server;
    private final ServerCommonNetworkHandler handler;
    private final NetworkHandlerExtension polymerHandler;
    private final ExtClientConnection extClientConnection;

    public PolymerHandshakeHandlerImplLate(MinecraftServer server, ServerCommonNetworkHandler handler) {
        this.server = server;
        this.handler = handler;
        this.polymerHandler = NetworkHandlerExtension.of(handler);
        this.extClientConnection = ExtClientConnection.of(handler);

        //PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c -> c.accept(this)));
    }

    public void sendPacket(Packet<?> packet) {
        this.handler.sendPacket(packet);
    }

    public void set(String polymerVersion, Object2IntMap<Identifier> protocolVersions) {
        this.extClientConnection.polymerNet$setVersion(polymerVersion);
        for (var entry : protocolVersions.object2IntEntrySet()) {
            this.extClientConnection.polymerNet$setSupportedVersion(entry.getKey(), entry.getIntValue());
        }
    }

    @Override
    public void setMetadataValue(Identifier identifier, NbtElement value) {
        this.extClientConnection.polymerNet$getMetadataMap().put(identifier, value);
    }

    public boolean isPolymer() {
        return this.extClientConnection.polymerNet$hasPolymer();
    }

    public String getPolymerVersion() {
        return this.extClientConnection.polymerNet$version();
    }

    public int getSupportedProtocol(Identifier identifier) {
        return this.extClientConnection.polymerNet$getSupportedVersion(identifier);
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
        if (this.handler instanceof PlayerAssociatedNetworkHandler playerAssociatedNetworkHandler) {
            return playerAssociatedNetworkHandler.getPlayer();
        }
        return null;
    }

    public static PolymerHandshakeHandler of(MinecraftServer server, ServerCommonNetworkHandler handler) {
        return new PolymerHandshakeHandlerImplLate(server, handler);
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        // No need to apply, as it applies by default!
        PolymerServerNetworking.ON_PLAY_SYNC.invoke(x -> x.accept(handler, this));
    }

    @Override
    public boolean getPackStatus() {
        return ((CommonClientConnectionExt) this.polymerHandler).polymerCommon$hasResourcePack();
    }

    @Override
    public void reset() {
        this.extClientConnection.polymerNet$getSupportMap().clear();
    }

    @Override
    public void setPackStatus(boolean status) {
        ((CommonClientConnectionExt) this.polymerHandler).polymerCommon$setResourcePack(status);
    }
}