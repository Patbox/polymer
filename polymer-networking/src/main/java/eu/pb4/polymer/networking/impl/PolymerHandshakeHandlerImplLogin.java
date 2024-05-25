package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonClientConnectionExt;
import eu.pb4.polymer.networking.api.server.EarlyConfigurationNetworkHandler;
import eu.pb4.polymer.networking.api.server.EarlyPlayNetworkHandler;
import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import eu.pb4.polymer.networking.impl.packets.HelloS2CPayload;
import eu.pb4.polymer.networking.impl.packets.MetadataPayload;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ApiStatus.Internal
public final class PolymerHandshakeHandlerImplLogin extends EarlyConfigurationNetworkHandler implements PolymerHandshakeHandler {
    public static int PING_ID = 0x91776;

    private int pings = 0;
    private final Object2LongMap<Identifier> lastUpdate = new Object2LongOpenHashMap<>();
    private final ExtClientConnection extClientConnection;

    private PolymerHandshakeHandlerImplLogin(Context context) {
        super(Identifier.of("polymer", "early_handshake"), context);
        this.sendPacket(new HelloS2CPayload());
        this.sendPing(PING_ID);
        this.extClientConnection = ExtClientConnection.of(this.getConnection());
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
        this.lastUpdate.put(identifier, System.currentTimeMillis());
    }

    @Override
    public long getLastPacketTime(Identifier identifier) {
        return this.lastUpdate.getLong(identifier);
    }

    @Override
    public @Nullable ServerPlayerEntity getPlayer() {
        return null;
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        var polymerHandler = NetworkHandlerExtension.of(handler);

        for (var entry : this.lastUpdate.keySet()) {
            polymerHandler.polymerNet$savePacketTime(entry);
        }
    }

    @Override
    public boolean getPackStatus(UUID uuid) {
        return ((CommonClientConnectionExt) this.getConnection()).polymerCommon$hasResourcePack(uuid);
    }

    @Override
    public void reset() {
        this.extClientConnection.polymerNet$getSupportMap().clear();
    }

    @Override
    public void setPackStatus(UUID uuid, boolean status) {
        ((CommonClientConnectionExt) this.getConnection()).polymerCommon$setResourcePack(uuid, status);
    }

    @Override
    public boolean handleCustomPayload(CustomPayloadC2SPacket packet) {
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
    public void onPong(CommonPongC2SPacket packet) {
        if (packet.getParameter() == PING_ID) {
            switch (this.pings++) {
                case 0 -> this.sendPing(PING_ID);
                case 1 -> this.continueJoining();
            }
        }
    }
}