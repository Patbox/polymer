package eu.pb4.polymer.networking.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import eu.pb4.polymer.networking.api.client.PolymerClientPacketHandler;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ServerPackets;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

import static eu.pb4.polymer.networking.api.PolymerServerNetworking.buf;

@ApiStatus.Internal
public class ClientPacketRegistry {
    public static final HashMap<Identifier, PolymerClientPacketHandler> PACKETS = new HashMap<>();
    public static final Object2IntMap<Identifier> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();
    public static String lastVersion;

    public static boolean handle(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        boolean versionRead = false;
        int version = -1;

        var packetHandler = PACKETS.get(identifier);


        if (packetHandler != null) {
            try {
                version = buf.readVarInt();
                versionRead = true;


                packetHandler.onPacket(handler, version, buf);
                return true;
            } catch (Throwable e) {
                CommonImpl.LOGGER.error(String.format("Invalid %s (%s) packet received from server!", identifier, versionRead ? version : "Unknown"), e);
            }
            return true;
        }

        return false;
    }

    public static void clear() {
        lastVersion = "";
        CLIENT_PROTOCOL.clear();
        PolymerClientNetworking.AFTER_DISABLE.invoke(Runnable::run);
    }

    public static boolean handleHandshake(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            lastVersion = buf.readString(64);
            CLIENT_PROTOCOL.clear();

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
            return true;
        }
        return false;
    }

    public static boolean handleDisable(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            MinecraftClient.getInstance().execute(() -> {
                clear();
            });
            return true;
        }
        return false;
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
    }
}
