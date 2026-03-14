package eu.pb4.polymer.networking.api.server;


import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.networking.impl.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class PolymerServerNetworking {
    private PolymerServerNetworking() {
    }

    public static final SimpleEvent<BiConsumer<ServerGamePacketListenerImpl, PolymerHandshakeHandler>> ON_PLAY_SYNC = new SimpleEvent<>();
    public static boolean send(ServerGamePacketListenerImpl handler, CustomPacketPayload payload) {
        handler.send(new ClientboundCustomPayloadPacket(payload));
        return true;
    }

    @Nullable
    public static <T extends Tag> T getMetadata(Connection handler, Identifier identifier, TagType<T> type) {
        var x = ExtConnection.of(handler).polymerNet$getMetadataMap().get(identifier);
        if (x != null && x.getType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    @Nullable
    public static <T extends Tag> T getMetadata(ServerCommonPacketListenerImpl handler, Identifier identifier, TagType<T> type) {
        var x = ExtConnection.of(handler).polymerNet$getMetadataMap().get(identifier);
        if (x != null && x.getType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    public static void setServerMetadata(Identifier identifier, @Nullable Tag nbtElement) {
        if (nbtElement == null) {
            ServerPacketRegistry.METADATA.remove(identifier);
        } else {
            ServerPacketRegistry.METADATA.put(identifier, nbtElement);
        }
    }

    public static <T extends CustomPacketPayload> void registerCommonHandler(Class<T> payloadClass, PolymerServerPacketHandler<ServerCommonPacketListenerImpl, T> handler) {
        ServerPacketRegistry.COMMON_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPacketPayload> void registerPlayHandler(Class<T> payloadClass, PolymerServerPacketHandler<ServerGamePacketListenerImpl, T> handler) {
        ServerPacketRegistry.PLAY_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPacketPayload> void registerConfigurationHandler(Class<T> payloadClass, PolymerServerPacketHandler<ServerConfigurationPacketListenerImpl, T> handler) {
        ServerPacketRegistry.CONFIG_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static int getSupportedVersion(ServerGamePacketListenerImpl handler, Identifier serverPacket) {
        return ExtConnection.of(handler).polymerNet$getSupportedVersion(serverPacket);
    }

    public static long getLastPacketReceivedTime(ServerGamePacketListenerImpl handler, Identifier identifier) {
        return ((PacketListenerImplExtension) handler).polymerNet$lastPacketUpdate(identifier);
    }
}
