package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.PolymerServerPacketHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

import static eu.pb4.polymer.networking.api.PolymerServerNetworking.buf;

@ApiStatus.Internal
public class ServerPacketRegistry {
    public static final HashMap<Identifier, PolymerServerPacketHandler> PACKETS = new HashMap<>();

    public static boolean handle(ServerPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        boolean versionRead = false;
        int version = -1;

        var packetHandler = PACKETS.get(identifier);


        if (packetHandler != null) {
            try {
                version = buf.readVarInt();
                versionRead = true;

                packetHandler.onPacket(handler, version, buf);
            } catch (Throwable e) {
                CommonImpl.LOGGER.error(String.format("Invalid %s (%s) packet received from client %s (%s)!", identifier, versionRead ? version : "Unknown", handler.getPlayer().getName().getString(), handler.getPlayer().getUuidAsString()), e);
            }
            return true;
        }

        return false;
    }

    public static void handleHandshake(PolymerHandshakeHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && !handler.isPolymer()) {
            var polymerVersion = buf.readString(64);
            var versionMap = new Object2IntOpenHashMap<Identifier>();

            var size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                var id = buf.readIdentifier();

                var size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                versionMap.put(id, ServerPackets.getBestSupported(id, list.elements()));
            }

            handler.getServer().execute(() -> {
                handler.set(polymerVersion, versionMap);
                handler.setLastPacketTime(ClientPackets.HANDSHAKE);

                sendHandshake(handler);
            });
        }
    }

    public static void sendHandshake(PolymerHandshakeHandler handler) {
        var buf = buf(0);

        buf.writeString(CommonImpl.VERSION);
        buf.writeVarInt(ClientPackets.REGISTRY.size());

        for (var id : ClientPackets.REGISTRY.keySet()) {
            buf.writeIdentifier(id);

            var entry = ClientPackets.REGISTRY.get(id);

            buf.writeVarInt(entry.length);
            for (int i : entry) {
                buf.writeVarInt(i);
            }
        }

        handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.HANDSHAKE, buf));
    }


    static {
        PACKETS.put(ClientPackets.HANDSHAKE, (handler, version, buf) -> handleHandshake(PolymerHandshakeHandler.of(handler), version, buf));
    }
}
