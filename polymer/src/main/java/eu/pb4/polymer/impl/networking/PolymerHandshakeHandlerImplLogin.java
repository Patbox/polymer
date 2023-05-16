package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.api.x.EarlyPlayNetworkHandler;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.interfaces.TempPlayerLoginAttachments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PolymerHandshakeHandlerImplLogin extends EarlyPlayNetworkHandler implements PolymerHandshakeHandler {
    public static long MAGIC_INIT_VALUE = 0xbb706c6d72627374L;
    public static int CONTINUE_LOGIN_ID = 1;

    private String polymerVersion = "";
    private Object2IntMap<String> protocolVersions = null;
    private Object2LongMap<String> lastUpdate = new Object2LongOpenHashMap<>();
    private BlockMapper blockMapper;
    private boolean hasPack = false;

    public PolymerHandshakeHandlerImplLogin(EarlyPlayNetworkHandler.Context context) {
        super(PolymerImplUtils.id("early_handshake"), context);
        ((TempPlayerLoginAttachments) this.getPlayer()).polymer_setHandshakeHandler(this);
        if (NetImpl.SEND_GAME_JOIN_PACKET) {
            this.sendInitialGameJoin();
        }
        this.sendKeepAlive(MAGIC_INIT_VALUE);
        this.blockMapper = BlockMapper.getDefault(this.getPlayer());

        PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c) -> c.accept(this));
    }

    public void set(String polymerVersion, Object2IntMap<String> protocolVersions) {
        this.polymerVersion = polymerVersion;
        this.protocolVersions = protocolVersions;
    }

    public boolean isPolymer() {
        return !this.polymerVersion.isEmpty();
    }

    public String getPolymerVersion() {
        return this.polymerVersion;
    }

    public int getSupportedProtocol(String identifier) {
        return this.protocolVersions != null ? this.protocolVersions.getOrDefault(identifier, -1) : -1;
    }

    @Override
    public void setLastPacketTime(String identifier) {
        this.lastUpdate.put(identifier, System.currentTimeMillis());
    }

    @Override
    public long getLastPacketTime(String identifier) {
        return this.lastUpdate.getLong(identifier);
    }

    @Override
    public boolean shouldUpdateWorld() {
        return false;
    }

    @Override
    public BlockMapper getBlockMapper() {
        return this.blockMapper;
    }

    @Override
    public void setBlockMapper(BlockMapper mapper) {
        this.blockMapper = mapper;
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        polymerHandler.polymer_setVersion(this.getPolymerVersion());

        if (this.protocolVersions != null) {
            for (var entry : this.protocolVersions.object2IntEntrySet()) {
                polymerHandler.polymer_setSupportedVersion(entry.getKey(), entry.getIntValue());
            }
        }

        for (var entry : this.lastUpdate.keySet()) {
            polymerHandler.polymer_savePacketTime(entry);
        }

        polymerHandler.polymer_setResourcePack(this.hasPack);

        polymerHandler.polymer_setBlockMapper(this.blockMapper);
    }

    @Override
    public boolean getPackStatus() {
        return this.hasPack;
    }

    @Override
    public void reset() {
        this.protocolVersions.clear();
    }

    @Override
    public void setPackStatus(boolean status) {
        this.hasPack = status;
    }

    @Override
    public boolean handleCustomPayload(CustomPayloadC2SPacket packet) {
        var data = packet.getData();
        if (packet.getChannel().equals(ClientPackets.HANDSHAKE_ID)) {
            PolymerServerProtocolHandler.handleHandshake(this, data.readVarInt(), data);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void handleKeepAlive(long value) {
        if (value == MAGIC_INIT_VALUE) {
            this.sendPing(CONTINUE_LOGIN_ID);
        }
    }

    @Override
    public void onPong(PlayPongC2SPacket packet) {
        if (packet.getParameter() == CONTINUE_LOGIN_ID) {
            this.continueJoining();
        }
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        if (PolymerRPUtils.shouldCheckByDefault()) {
            this.hasPack = switch (packet.getStatus()) {
                case ACCEPTED, SUCCESSFULLY_LOADED -> true;
                case DECLINED, FAILED_DOWNLOAD -> false;
            };
        }
    }

}
