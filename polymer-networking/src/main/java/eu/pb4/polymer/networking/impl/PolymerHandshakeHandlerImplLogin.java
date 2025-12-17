package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonConnectionExt;
import eu.pb4.polymer.networking.api.server.EarlyConfigurationNetworkHandler;
import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import eu.pb4.polymer.networking.impl.packets.HelloS2CPayload;
import eu.pb4.polymer.networking.impl.packets.MetadataPayload;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@ApiStatus.Internal
public final class PolymerHandshakeHandlerImplLogin extends EarlyConfigurationNetworkHandler implements PolymerHandshakeHandler {
    public static int PING_ID = 0x91776;

    private int pings = 0;
    private final Object2LongMap<Identifier> lastUpdate = new Object2LongOpenHashMap<>();
    private final ExtConnection extClientConnection;

    private PolymerHandshakeHandlerImplLogin(Context context) {
        super(Identifier.fromNamespaceAndPath("polymer", "early_handshake"), context);
        this.sendPacket(new HelloS2CPayload());
        this.sendPing(PING_ID);
        this.extClientConnection = ExtConnection.of(this.getConnection());
    }

    @Nullable
    public static EarlyConfigurationNetworkHandler create(Context context) {
        if (PolymerCommonUtils.isBedrockPlayer(context.profile())) {
            return null;
        }
        return new PolymerHandshakeHandlerImplLogin(context);
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
        this.lastUpdate.put(identifier, System.currentTimeMillis());
    }

    @Override
    public long getLastPacketTime(Identifier identifier) {
        return this.lastUpdate.getLong(identifier);
    }

    @Override
    public @Nullable ServerPlayer getPlayer() {
        return null;
    }

    @Override
    public void apply(ServerGamePacketListenerImpl handler) {
        var polymerHandler = PacketListenerImplExtension.of(handler);

        for (var entry : this.lastUpdate.keySet()) {
            polymerHandler.polymerNet$savePacketTime(entry);
        }
    }

    @Override
    public boolean getPackStatus(UUID uuid) {
        return ((CommonConnectionExt) this.getConnection()).polymerCommon$hasResourcePack(uuid);
    }

    @Override
    public void reset() {
        this.extClientConnection.polymerNet$getSupportMap().clear();
    }

    @Override
    public void setPackStatus(UUID uuid, boolean status) {
        ((CommonConnectionExt) this.getConnection()).polymerCommon$setResourcePack(uuid, status);
    }

    @Override
    public boolean tryHandleCustomPayload(ServerboundCustomPayloadPacket packet) {
        if (packet.payload() instanceof HandshakePayload handshakePayload) {
            try {
                ServerPacketRegistry.handleHandshake(this, handshakePayload);
            } catch (Throwable e) {
                NetImpl.LOGGER.error("Packet Handling failed!", e);
            }
            return true;
        }  if (packet.payload() instanceof MetadataPayload payload) {
            try {
                ServerPacketRegistry.handleMetadata(this, payload);
            } catch (Throwable e) {
                NetImpl.LOGGER.error("Packet Handling failed!", e);
            }
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void handlePong(ServerboundPongPacket packet) {
        if (packet.getId() == PING_ID) {
            switch (this.pings++) {
                case 0 -> this.sendPing(PING_ID);
                case 1 -> this.continueJoining();
            }
        }
    }
}