package eu.pb4.polymer.networking.api.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.NetworkHandlerExtension;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import eu.pb4.polymer.networking.impl.ServerPackets;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


/**
 * General utilities while dealing with client side integrations
 */
@Environment(EnvType.CLIENT)
public final class PolymerClientNetworking {
    public static final SimpleEvent<Runnable> AFTER_HANDSHAKE_RECEIVED = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> AFTER_DISABLE = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> BEFORE_METADATA_SYNC = new SimpleEvent<>();

    private PolymerClientNetworking() {
    }


    public static boolean sendDirect(ClientPlayNetworkHandler player, Identifier identifier, PacketByteBuf packetByteBuf) {
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

    public static boolean registerSendPacket(Identifier identifier, int... supportedVersions) {
        ClientPackets.register(identifier, supportedVersions);
        return true;
    }

    public static int getSupportedVersion(Identifier identifier) {
        return ClientPacketRegistry.CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    @Nullable
    public static <T extends NbtElement> T getMetadata(Identifier identifier, NbtType<T> type) {
        var x = ClientPacketRegistry.SERVER_METADATA.get(identifier);
        if (x != null && x.getNbtType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    public static void setClientMetadata(Identifier identifier, @Nullable NbtElement nbtElement) {
        if (nbtElement == null) {
            ClientPacketRegistry.METADATA.remove(identifier);
        } else {
            ClientPacketRegistry.METADATA.put(identifier, nbtElement);
        }
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
