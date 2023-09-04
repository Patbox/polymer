package eu.pb4.polymer.networking.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import eu.pb4.polymer.networking.api.client.PolymerClientPacketHandler;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import eu.pb4.polymer.networking.impl.ServerPackets;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

import static eu.pb4.polymer.networking.api.PolymerServerNetworking.buf;

@ApiStatus.Internal
public class ClientPacketRegistry {
    public static final HashMap<Identifier, PolymerClientPacketHandler> PACKETS = new HashMap<>();
    public static final Object2IntMap<Identifier> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();
    public static final Map<Identifier, NbtElement> SERVER_METADATA = new HashMap<>();
    public static final Map<Identifier, NbtElement> METADATA = new HashMap<>();
    public static String lastVersion;

    public static boolean handle(ClientPlayNetworkHandler handler, CustomPayloadS2CPacket packet) {
        boolean versionRead = false;
        int version = -1;

        var identifier = packet.getChannel();

        var packetHandler = PACKETS.get(identifier);


        if (packetHandler != null) {
            var buf = packet.getData();
            try {
                version = buf.readVarInt();
                versionRead = true;

                packetHandler.onPacket(handler, version, buf);
                return true;
            } catch (Throwable e) {
                CommonImpl.LOGGER.error(String.format("Invalid %s (%s) packet received from server!", identifier, versionRead ? version : "Unknown"), e);
            }
            try {
                buf.release();
            } catch (Throwable e) {}
            return true;
        }

        return false;
    }

    public static void clear() {
        lastVersion = "";
        CLIENT_PROTOCOL.clear();
        synchronized (SERVER_METADATA) {
            SERVER_METADATA.clear();
        }
        PolymerClientNetworking.AFTER_DISABLE.invoke(Runnable::run);
    }

    public static void handleMetadata(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            synchronized (SERVER_METADATA) {
                try {
                    ServerPacketRegistry.decodeMetadata(buf, SERVER_METADATA::put);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        PolymerClientNetworking.AFTER_METADATA_RECEIVED.invoke(Runnable::run);
    }

    public static void handleHandshake(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            lastVersion = buf.readString(64);
            CLIENT_PROTOCOL.clear();
            SERVER_METADATA.clear();

            var size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                var id = buf.readIdentifier();

                var size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                CLIENT_PROTOCOL.put(id, ClientPackets.getBestSupported(id, list.elements()));
            }

            PolymerClientNetworking.AFTER_HANDSHAKE_RECEIVED.invoke(Runnable::run);

            if (CLIENT_PROTOCOL.getOrDefault(ClientPackets.METADATA, -1) != -1 ) {
                sendMetadata(handler);
            } else {
                PolymerClientNetworking.AFTER_METADATA_RECEIVED.invoke(Runnable::run);
            }
        }
    }

    private static void sendMetadata(ClientPlayNetworkHandler handler) {
        try {
            PolymerClientNetworking.BEFORE_METADATA_SYNC.invoke(Runnable::run);
            var buf = buf(1);
            ServerPacketRegistry.encodeMetadata(buf, METADATA);
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.METADATA, buf));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void handleDisable(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            clear();
        }
    }

    public static void sendHandshake(ClientPlayNetworkHandler handler) {
            var buf = buf(0);

            buf.writeString(CommonImpl.VERSION);
            buf.writeVarInt(ServerPackets.REGISTRY.size());

            for (var id : ServerPackets.REGISTRY.keySet()) {
                buf.writeIdentifier(id);

                var entry = ServerPackets.REGISTRY.get(id);

                buf.writeVarInt(entry.length);

                for (int i : entry) {
                    buf.writeVarInt(i);
                }
            }

            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.HANDSHAKE, buf));
    }


    static {
        PACKETS.put(ServerPackets.HANDSHAKE, ClientPacketRegistry::handleHandshake);
        PACKETS.put(ServerPackets.DISABLE, ClientPacketRegistry::handleDisable);
        PACKETS.put(ServerPackets.METADATA, ClientPacketRegistry::handleMetadata);
    }
}
