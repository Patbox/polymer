package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ServerPacketHandler {
    public static void handle(ServerPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        switch (identifier.getPath()) {
            case PolymerPacketIds.VERSION -> {

                var ver = polymerHandler.polymer_protocolVersion();
                polymerHandler.polymer_setVersion(Math.max(buf.readShort(), -1), buf.readString(64));

                if (System.currentTimeMillis() - polymerHandler.polymer_lastSyncUpdate() > 1000 * 20) {
                    polymerHandler.polymer_saveSyncTime();
                    ServerPacketBuilders.createSyncPackets(handler);

                    if (ver == -2 && handler.getPlayer() != null) {
                        var world = handler.getPlayer().getServerWorld();
                        int dist = ((ThreadedAnvilChunkStorageAccessor) handler.getPlayer().getServerWorld().getChunkManager().threadedAnvilChunkStorage).getWatchDistance();
                        int playerX = handler.player.getWatchedSection().getX();
                        int playerZ = handler.player.getWatchedSection().getZ();

                        for (int x = -dist; x <= dist; x++) {
                            for (int z = -dist; z <= dist; z++) {
                                var chunk = (WorldChunk) world.getChunk(x + playerX, z + playerZ, ChunkStatus.FULL, false);

                                if (chunk != null) {
                                    ServerPacketBuilders.createChunkPacket(handler, null, chunk);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
