package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.PolymerServerPacketHandler;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtTagSizeTracker;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static eu.pb4.polymer.networking.api.PolymerServerNetworking.buf;

@ApiStatus.Internal
public class ServerPacketRegistry {
    public static final HashMap<Identifier, PolymerServerPacketHandler> PACKETS = new HashMap<>();
    public static final HashMap<Identifier, NbtElement> METADATA = new HashMap<>();

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
                if (handler.getSupportedProtocol(ServerPackets.METADATA) > -1) {
                    sendMetadata(handler);
                }
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

    private static void sendMetadata(PolymerHandshakeHandler handler) {
        try {
            var buf = buf(1);
            ServerPacketRegistry.encodeMetadata(buf, METADATA);
            handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.METADATA, buf));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void handleMetadata(PolymerHandshakeHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            try {
                decodeMetadata(buf, handler::setMetadataValue);
            } catch (Throwable e) {

            }
        }
    }

    static {
        PACKETS.put(ClientPackets.HANDSHAKE, (handler, version, buf) -> handleHandshake(PolymerHandshakeHandler.of(handler), version, buf));
        PACKETS.put(ClientPackets.METADATA, (handler, version, buf) -> handleMetadata(PolymerHandshakeHandler.of(handler), version, buf));
    }



    public static void decodeMetadata(PacketByteBuf buf, BiConsumer<Identifier, NbtElement> map) throws Exception {
        var size = buf.readVarInt();
        var str = new ByteBufInputStream(buf);
        for (int i = 0; i < size; i++) {
            var id = buf.readIdentifier();
            var type= NbtTypes.byId(buf.readByte());
            var data = type.read(str, 0, NbtTagSizeTracker.EMPTY);
            map.accept(id, data);
        }
    }

    public static void encodeMetadata(PacketByteBuf buf, Map<Identifier, NbtElement> map) throws Exception {
        buf.writeVarInt(map.size());
        var str = new ByteBufOutputStream(buf);
        for (var e : map.entrySet()) {
            buf.writeIdentifier(e.getKey());
            buf.writeByte(e.getValue().getType());
            e.getValue().write(str);
        }
    }
}
