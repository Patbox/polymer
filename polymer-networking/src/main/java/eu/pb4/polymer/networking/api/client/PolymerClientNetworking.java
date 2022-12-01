package eu.pb4.polymer.networking.api.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ServerPackets;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;


/**
 * General utilities while dealing with client side integrations
 */
@Environment(EnvType.CLIENT)
public final class PolymerClientNetworking {
    public static final SimpleEvent<Runnable> AFTER_HANDSHAKE_RECEIVED = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> AFTER_DISABLE = new SimpleEvent<>();

    private PolymerClientNetworking() {
    }


    public static boolean sendPacket(ClientPlayNetworkHandler player, Identifier identifier, PacketByteBuf packetByteBuf) {
        player.sendPacket(new CustomPayloadC2SPacket(identifier, packetByteBuf));
        return true;
    }

    public static boolean registerPacketHandler(Identifier identifier, PolymerClientPacketHandler handler) {
        return registerPacketHandler(identifier, handler, ServerPackets.REGISTRY.getOrDefault(identifier, new int[0]));
    }

    public static boolean registerPacketHandler(Identifier identifier, PolymerClientPacketHandler handler, int... supportedVersions) {
        if (!ClientPacketRegistry.PACKETS.containsKey(identifier)) {
            ServerPackets.register(identifier, supportedVersions);
            ClientPacketRegistry.PACKETS.put(identifier, handler);
            return true;
        }
        return false;
    }

    public static boolean registerClientPacket(Identifier identifier, int... supportedVersions) {
        ClientPackets.register(identifier, supportedVersions);
        return true;
    }

    public static int getSupportedVersion(Identifier identifier) {
        return ClientPacketRegistry.CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    static {
        ClientPackets.register();
        ServerPackets.register();
    }

    public static String getServerVersion() {
        return ClientPacketRegistry.lastVersion;
    }

    public static boolean isEnabled() {
        return ClientPacketRegistry.lastVersion.isEmpty();
    }
}
