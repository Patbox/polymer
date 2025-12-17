package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonConnectionExt;
import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.Internal
public class PolymerHandshakeHandlerImplLate implements PolymerHandshakeHandler {
    private final MinecraftServer server;
    private final ServerCommonPacketListenerImpl handler;
    private final PacketListenerImplExtension polymerHandler;
    private final ExtConnection extClientConnection;

    public PolymerHandshakeHandlerImplLate(MinecraftServer server, ServerCommonPacketListenerImpl handler) {
        this.server = server;
        this.handler = handler;
        this.polymerHandler = PacketListenerImplExtension.of(handler);
        this.extClientConnection = ExtConnection.of(handler);

        //PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c -> c.accept(this)));
    }

    public void sendPacket(Packet<?> packet) {
        this.handler.send(packet);
    }

    public void set(String polymerVersion, Object2IntMap<Identifier> protocolVersions) {
        this.extClientConnection.polymerNet$setVersion(polymerVersion);
        for (var entry : protocolVersions.object2IntEntrySet()) {
            this.extClientConnection.polymerNet$setSupportedVersion(entry.getKey(), entry.getIntValue());
        }
    }

    @Override
    public void setMetadataValue(Identifier identifier, Tag value) {
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

    public ServerPlayer getPlayer() {
        if (this.handler instanceof ServerPlayerConnection playerAssociatedNetworkHandler) {
            return playerAssociatedNetworkHandler.getPlayer();
        }
        return null;
    }

    public static PolymerHandshakeHandler of(MinecraftServer server, ServerCommonPacketListenerImpl handler) {
        return new PolymerHandshakeHandlerImplLate(server, handler);
    }

    @Override
    public void apply(ServerGamePacketListenerImpl handler) {
        // No need to apply, as it applies by default!
        PolymerServerNetworking.ON_PLAY_SYNC.invoke(x -> x.accept(handler, this));
    }

    @Override
    public boolean getPackStatus(UUID uuid) {
        return ((CommonConnectionExt) this.polymerHandler).polymerCommon$hasResourcePack(uuid);
    }

    @Override
    public void reset() {
        this.extClientConnection.polymerNet$getSupportMap().clear();
    }

    @Override
    public void setPackStatus(UUID uuid, boolean status) {
        ((CommonConnectionExt) this.polymerHandler).polymerCommon$setResourcePack(uuid, status);
    }
}