package eu.pb4.polymer.api.networking;

import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.networking.ClientPackets;
import eu.pb4.polymer.impl.networking.PolymerServerProtocolHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class PolymerPacketUtils {
    private PolymerPacketUtils() {
    }

    private static final Map<Identifier, String> MAP = new HashMap<>();
    private static final Map<Identifier, Identifier> MAP_ID_CLIENT = new HashMap<>();

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

    public static boolean sendPacket(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf packetByteBuf) {
        var packetName = MAP_ID_CLIENT.get(identifier);
        if (packetName == null) {
            packetName = PolymerImplUtils.id("custom/" + identifier.getNamespace() + "/" + identifier.getPath());
            MAP_ID_CLIENT.put(identifier, packetName);
        }
        handler.sendPacket(new CustomPayloadS2CPacket(packetName, packetByteBuf));
        return true;
    }

    public static boolean registerPacket(Identifier identifier, PolymerServerPacketHandler handler, int... supportedVersions) {
        if (!MAP.containsKey(identifier)) {
            var packet = "custom/" + identifier.getNamespace() + "/" + identifier.getPath();
            MAP.put(identifier, packet);

            ClientPackets.register(packet, supportedVersions);
            PolymerServerProtocolHandler.CUSTOM_PACKETS.put(packet, handler);
            return true;
        }
        return false;
    }
}
