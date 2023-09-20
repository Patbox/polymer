package eu.pb4.polymer.networking.api.server;


import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.networking.impl.*;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public final class PolymerServerNetworking {
    private PolymerServerNetworking() {
    }

    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, PolymerHandshakeHandler>> ON_PLAY_SYNC = new SimpleEvent<>();

    public static boolean send(ServerPlayNetworkHandler handler, CustomPayload payload) {
        handler.sendPacket(new CustomPayloadS2CPacket(payload));
        return true;
    }

    @Nullable
    public static <T extends NbtElement> T getMetadata(ServerCommonNetworkHandler handler, Identifier identifier, NbtType<T> type) {
        var x = ExtClientConnection.of(handler).polymerNet$getMetadataMap().get(identifier);
        if (x != null && x.getNbtType() == type) {
            //noinspection unchecked
            return (T) x;
        }
        return null;
    }

    public static void setServerMetadata(Identifier identifier, @Nullable NbtElement nbtElement) {
        if (nbtElement == null) {
            ServerPacketRegistry.METADATA.remove(identifier);
        } else {
            ServerPacketRegistry.METADATA.put(identifier, nbtElement);
        }
    }

    public static <T extends CustomPayload> void registerCommonHandler(Class<T> payloadClass, PolymerServerPacketHandler<ServerCommonNetworkHandler, T> handler) {
        ServerPacketRegistry.COMMON_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPayload> void registerPlayHandler(Class<T> payloadClass, PolymerServerPacketHandler<ServerPlayNetworkHandler, T> handler) {
        ServerPacketRegistry.PLAY_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static <T extends CustomPayload> void registerConfigurationHandler(Class<T> payloadClass, PolymerServerPacketHandler<ServerConfigurationNetworkHandler, T> handler) {
        ServerPacketRegistry.CONFIG_PACKET_LISTENERS.computeIfAbsent(payloadClass, (x) -> new ArrayList<>()).add(handler);
    }

    public static int getSupportedVersion(ServerPlayNetworkHandler handler, Identifier serverPacket) {
        return ExtClientConnection.of(handler).polymerNet$getSupportedVersion(serverPacket);
    }

    public static long getLastPacketReceivedTime(ServerPlayNetworkHandler handler, Identifier identifier) {
        return ((NetworkHandlerExtension) handler).polymerNet$lastPacketUpdate(identifier);
    }
}
