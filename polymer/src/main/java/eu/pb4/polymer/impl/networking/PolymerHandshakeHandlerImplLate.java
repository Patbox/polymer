package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.block.BlockMapper;
import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PolymerHandshakeHandlerImplLate implements PolymerHandshakeHandler {
    private final MinecraftServer server;
    private final ServerPlayNetworkHandler handler;
    private final PolymerNetworkHandlerExtension polymerHandler;

    public PolymerHandshakeHandlerImplLate(MinecraftServer server, ServerPlayNetworkHandler handler) {
        this.server = server;
        this.handler = handler;
        this.polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c -> c.accept(this)));
    }

    public void sendPacket(Packet<?> packet) {
        this.handler.sendPacket(packet);
    }

    public void set(String polymerVersion, Object2IntMap<String> protocolVersions) {
        this.polymerHandler.polymer$setVersion(polymerVersion);
        for (var entry : protocolVersions.object2IntEntrySet()) {
            this.polymerHandler.polymer$setSupportedVersion(entry.getKey(), entry.getIntValue());
        }
    }

    public boolean isPolymer() {
        return this.polymerHandler.polymer$hasPolymer();
    }

    public String getPolymerVersion() {
        return this.polymerHandler.polymer$version();
    }

    public int getSupportedProtocol(String identifier) {
        return this.polymerHandler.polymer$getSupportedVersion(identifier);
    }

    @Override
    public void setLastPacketTime(String identifier) {
        this.polymerHandler.polymer$savePacketTime(identifier);
    }

    @Override
    public long getLastPacketTime(String identifier) {
        return this.polymerHandler.polymer$lastPacketUpdate(identifier);

    }

    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public boolean shouldUpdateWorld() {
        return true;
    }

    @Override
    public BlockMapper getBlockMapper() {
        return this.polymerHandler.polymer$getBlockMapper();
    }

    @Override
    public void setBlockMapper(BlockMapper mapper) {
        this.polymerHandler.polymer$setBlockMapper(mapper);
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
    }

    @Override
    public boolean getPackStatus() {
        return this.polymerHandler.polymer$hasResourcePack();
    }

    @Override
    public void reset() {
        this.polymerHandler.polymer$resetSupported();
    }

    @Override
    public void setPackStatus(boolean status) {
        this.polymerHandler.polymer$setResourcePack(status);
    }
}