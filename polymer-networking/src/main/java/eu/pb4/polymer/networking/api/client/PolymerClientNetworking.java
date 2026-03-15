package eu.pb4.polymer.networking.api.client;

import eu.pb4.polymer.common.impl.EventImplUtils;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;


/**
 * General utilities while dealing with client side integrations
 */
@Environment(EnvType.CLIENT)
public final class PolymerClientNetworking {
    public static final Event<Runnable> AFTER_HANDSHAKE_RECEIVED = EventImplUtils.createRunnableEvent();
    public static final Event<Runnable> AFTER_METADATA_RECEIVED = EventImplUtils.createRunnableEvent();
    public static final Event<Runnable> AFTER_DISABLE = EventImplUtils.createRunnableEvent();
    public static final Event<Runnable> BEFORE_METADATA_SYNC = EventImplUtils.createRunnableEvent();

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
