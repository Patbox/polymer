package eu.pb4.polymer.core.api.networking;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.networking.ClientPackets;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocolHandler;
import eu.pb4.polymer.core.impl.networking.ServerPackets;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class PolymerPacketUtils {
    private PolymerPacketUtils() {
    }

    private static final Map<Identifier, String> MAP_C2S = new HashMap<>();
    private static final Map<Identifier, Identifier> MAP_S2C = new HashMap<>();

    /**
     * Creates versioned PacketByteBuf
     *
     * @param version Packet version
     * @return PacketByteBuf
     */
    public static PacketByteBuf buf(int version) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        return buf.writeVarInt(version);
    }

    public static boolean sendPacket(ServerPlayNetworkHandler handler, Identifier identifier, PacketByteBuf packetByteBuf) {
        var packetName = MAP_S2C.get(identifier);
        if (packetName == null) {
            packetName = PolymerImplUtils.id("custom/" + identifier.getNamespace() + "/" + identifier.getPath());
            MAP_S2C.put(identifier, packetName);
        }
        handler.sendPacket(new CustomPayloadS2CPacket(packetName, packetByteBuf));
        return true;
    }

    public static boolean registerPacketHandler(Identifier identifier, PolymerServerPacketHandler handler, int... supportedVersions) {
        if (!MAP_C2S.containsKey(identifier)) {
            var packet = "custom/" + identifier.getNamespace() + "/" + identifier.getPath();
            MAP_C2S.put(identifier, packet);

            ClientPackets.register(packet, supportedVersions);
            PolymerServerProtocolHandler.CUSTOM_PACKETS.put(packet, handler);
            return true;
        }
        return false;
    }

    public static boolean registerServerPacket(Identifier identifier, int... supportedVersions) {
        ServerPackets.register("custom/" + identifier.getNamespace() + "/" + identifier.getPath(), supportedVersions);
        return true;
    }

    public static int getSupportedVersion(ServerPlayNetworkHandler handler, Identifier identifier) {
        var packetName = MAP_S2C.get(identifier);
        if (packetName == null) {
            packetName = PolymerImplUtils.id("custom/" + identifier.getNamespace() + "/" + identifier.getPath());
            MAP_S2C.put(identifier, packetName);
        }
        return ((PolymerNetworkHandlerExtension) handler).polymer$getSupportedVersion(packetName.getPath());
    }
}
