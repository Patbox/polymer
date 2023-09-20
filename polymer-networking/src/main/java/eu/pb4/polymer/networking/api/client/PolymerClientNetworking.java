package eu.pb4.polymer.networking.api.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ServerPackets;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


/**
 * General utilities while dealing with client side integrations
 */
@Environment(EnvType.CLIENT)
public final class PolymerClientNetworking {
    public static final SimpleEvent<Runnable> AFTER_HANDSHAKE_RECEIVED = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> AFTER_METADATA_RECEIVED = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> AFTER_DISABLE = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> BEFORE_METADATA_SYNC = new SimpleEvent<>();

    private PolymerClientNetworking() {
    }

    public static <T extends CustomPayload> void registerCommonHandler(Class<T> payloadClass, PolymerClientPacketHandler<ClientCommonNetworkHandler, T> handler) {
        ClientPacketRegistry.COMMON_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPayload> void registerPlayHandler(Class<T> payloadClass, PolymerClientPacketHandler<ClientPlayNetworkHandler, T> handler) {
        ClientPacketRegistry.PLAY_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPayload> void registerConfigurationHandler(Class<T> payloadClass, PolymerClientPacketHandler<ClientConfigurationNetworkHandler, T> handler) {
        ClientPacketRegistry.CONFIG_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
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

    public static String getServerVersion() {
        return ClientPacketRegistry.lastVersion;
    }

    public static boolean isEnabled() {
        return ClientPacketRegistry.lastVersion.isEmpty();
    }
}
