package eu.pb4.polymer.networking.api.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ServerPackets;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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

    public static <T extends CustomPacketPayload> void registerCommonHandler(Class<T> payloadClass, PolymerClientPacketHandler<ClientCommonPacketListenerImpl, T> handler) {
        ClientPacketRegistry.COMMON_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPacketPayload> void registerPlayHandler(Class<T> payloadClass, PolymerClientPacketHandler<ClientPacketListener, T> handler) {
        ClientPacketRegistry.PLAY_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPacketPayload> void registerConfigurationHandler(Class<T> payloadClass, PolymerClientPacketHandler<ClientConfigurationPacketListenerImpl, T> handler) {
        ClientPacketRegistry.CONFIG_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static int getSupportedVersion(Identifier identifier) {
        return ClientPacketRegistry.CLIENT_PROTOCOL.getOrDefault(identifier, -1);
    }

    @Nullable
    public static <T extends Tag> T getMetadata(Identifier identifier, TagType<T> type) {
        var x = ClientPacketRegistry.SERVER_METADATA.get(identifier);
        if (x != null && x.getType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    public static void setClientMetadata(Identifier identifier, @Nullable Tag nbtElement) {
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
