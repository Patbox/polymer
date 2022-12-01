package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import eu.pb4.polymer.networking.api.EarlyPlayNetworkHandler;
import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerHandshakeHandlerImplLogin extends EarlyPlayNetworkHandler implements PolymerHandshakeHandler {
    public static long MAGIC_VALUE = 0xbb706c6d72627374L;

    private String polymerVersion = "";
    private Object2IntMap<Identifier> protocolVersions = null;
    private final Object2LongMap<Identifier> lastUpdate = new Object2LongOpenHashMap<>();

    public PolymerHandshakeHandlerImplLogin(Context context) {
        super(new Identifier("polymer", "early_handshake"), context);
        ((TempPlayerLoginAttachments) this.getPlayer()).polymer$setHandshakeHandler(this);
        this.sendKeepAlive(MAGIC_VALUE);

        //PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c) -> c.accept(this));
    }

    public void set(String polymerVersion, Object2IntMap<Identifier> protocolVersions) {
        this.polymerVersion = polymerVersion;
        this.protocolVersions = protocolVersions;
    }

    public boolean isPolymer() {
        return !this.polymerVersion.isEmpty();
    }

    public String getPolymerVersion() {
        return this.polymerVersion;
    }

    public int getSupportedProtocol(Identifier identifier) {
        return this.protocolVersions != null ? this.protocolVersions.getOrDefault(identifier, -1) : -1;
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
    public void apply(ServerPlayNetworkHandler handler) {
        var polymerHandler = NetworkHandlerExtension.of(handler);

        polymerHandler.polymer$setVersion(this.getPolymerVersion());

        if (this.protocolVersions != null) {
            for (var entry : this.protocolVersions.object2IntEntrySet()) {
                polymerHandler.polymer$setSupportedVersion(entry.getKey(), entry.getIntValue());
            }
        }

        for (var entry : this.lastUpdate.keySet()) {
            polymerHandler.polymer$savePacketTime(entry);
        }
    }

    @Override
    public boolean getPackStatus() {
        return ((CommonResourcePackInfoHolder) this.getPlayer()).polymerCommon$hasResourcePack();
    }

    @Override
    public void reset() {
        this.protocolVersions.clear();
    }

    @Override
    public void setPackStatus(boolean status) {
        ((CommonResourcePackInfoHolder) this.getPlayer()).polymerCommon$setResourcePack(status);
    }

    @Override
    public boolean handleCustomPayload(CustomPayloadC2SPacket packet) {
        var data = packet.getData();
        if (packet.getChannel().equals(ClientPackets.HANDSHAKE)) {
            try {
                ServerPacketRegistry.handleHandshake(this, data.readVarInt(), data);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void handleKeepAlive(long value) {
        if (value == MAGIC_VALUE) {
            this.continueJoining();
        }
    }
}