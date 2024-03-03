package eu.pb4.polymer.networking.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonNetworkHandlerExt;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import eu.pb4.polymer.networking.api.client.PolymerClientPacketHandler;
import eu.pb4.polymer.networking.api.payload.SingleplayerSerialization;
import eu.pb4.polymer.networking.impl.*;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import eu.pb4.polymer.networking.impl.packets.HelloS2CPayload;
import eu.pb4.polymer.networking.impl.packets.MetadataPayload;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;

import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class ClientPacketRegistry {
    public static final HashMap<Class<?>, List<PolymerClientPacketHandler<ClientCommonNetworkHandler, ?>>> COMMON_PACKET_LISTENERS = new HashMap<>();
    public static final HashMap<Class<?>, List<PolymerClientPacketHandler<ClientPlayNetworkHandler, ?>>> PLAY_PACKET_LISTENERS = new HashMap<>();
    public static final HashMap<Class<?>, List<PolymerClientPacketHandler<ClientConfigurationNetworkHandler, ?>>> CONFIG_PACKET_LISTENERS = new HashMap<>();
    public static final Object2IntMap<Identifier> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();
    public static final Map<Identifier, NbtElement> SERVER_METADATA = new HashMap<>();
    public static final Map<Identifier, NbtElement> METADATA = new HashMap<>();
    public static String lastVersion;
    public static void register() {
        PolymerClientNetworking.registerCommonHandler(HandshakePayload.class, ClientPacketRegistry::handleHandshake);
        PolymerClientNetworking.registerCommonHandler(MetadataPayload.class, ClientPacketRegistry::handleMetadata);
        PolymerClientNetworking.registerCommonHandler(DisableS2CPayload.class, ClientPacketRegistry::handleDisable);
        PolymerClientNetworking.registerCommonHandler(HelloS2CPayload.class, ClientPacketRegistry::handleHello);
    }

    private static void handleHello(MinecraftClient client, ClientCommonNetworkHandler handler, HelloS2CPayload payload) {
        sendHandshake(handler);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "UnstableApiUsage"})
    public static boolean handle(MinecraftClient client, ClientCommonNetworkHandler handler, CustomPayload packet) {
        var packetHandlers = COMMON_PACKET_LISTENERS.get(packet.getClass());
        boolean handled = false;
        if (packetHandlers != null) {
            for (var pHandler : packetHandlers) {
                ((PolymerClientPacketHandler) pHandler).onPacket(client, handler, packet);
            }
            handled = !packetHandlers.isEmpty();
        }

        if (handler instanceof ClientPlayNetworkHandler playNetworkHandler) {
            var packetHandlers2 = PLAY_PACKET_LISTENERS.get(packet.getClass());
            if (packetHandlers2 != null) {
                for (var pHandler : packetHandlers2) {
                    ((PolymerClientPacketHandler) pHandler).onPacket(client, playNetworkHandler, packet);
                }
                handled = handled || !packetHandlers2.isEmpty();
            }
        } else if (handler instanceof ClientConfigurationNetworkHandler networkHandler) {
            var packetHandlers2 = CONFIG_PACKET_LISTENERS.get(packet.getClass());
            if (packetHandlers2 != null) {
                for (var pHandler : packetHandlers2) {
                    ((PolymerClientPacketHandler) pHandler).onPacket(client, networkHandler, packet);
                }
                handled = handled || !packetHandlers2.isEmpty();
            }
        }

        return handled;
    }

    public static void clear(@Nullable ClientCommonNetworkHandler handler) {
        lastVersion = "";
        CLIENT_PROTOCOL.clear();
        synchronized (SERVER_METADATA) {
            SERVER_METADATA.clear();
        }
        if (handler != null) {
            var ext = (ExtClientConnection) ((CommonNetworkHandlerExt) handler).polymerCommon$getConnection();
            ext.polymerNet$getMetadataMap().clear();
            ext.polymerNet$getSupportMap().clear();
            ext.polymerNet$setVersion("");
        }

        PolymerClientNetworking.AFTER_DISABLE.invoke(Runnable::run);
    }

    public static void handleMetadata(MinecraftClient client, ClientCommonNetworkHandler handler, MetadataPayload payload) {
        synchronized (SERVER_METADATA) {
            SERVER_METADATA.clear();
            SERVER_METADATA.putAll(payload.map());
        }

        var ext = (ExtClientConnection) ((CommonNetworkHandlerExt) handler).polymerCommon$getConnection();
        ext.polymerNet$getMetadataMap().clear();
        ext.polymerNet$getMetadataMap().putAll(payload.map());

        PolymerClientNetworking.AFTER_METADATA_RECEIVED.invoke(Runnable::run);
    }

    public static void handleHandshake(MinecraftClient client, ClientCommonNetworkHandler handler, HandshakePayload payload) {
        CLIENT_PROTOCOL.clear();
        SERVER_METADATA.clear();

        lastVersion = payload.version();

        payload.packetVersions().forEach((id, ver) -> CLIENT_PROTOCOL.put(id, ClientPackets.getBestSupported(id, ver)));

        var ext = (ExtClientConnection) ((CommonNetworkHandlerExt) handler).polymerCommon$getConnection();
        ext.polymerNet$getSupportMap().putAll(CLIENT_PROTOCOL);
        ext.polymerNet$setVersion(lastVersion);

        PolymerClientNetworking.AFTER_HANDSHAKE_RECEIVED.invoke(Runnable::run);
        sendMetadata(handler);
    }

    private static void sendMetadata(ClientCommonNetworkHandler handler) {
        try {
            PolymerClientNetworking.BEFORE_METADATA_SYNC.invoke(Runnable::run);
            handler.sendPacket(new CustomPayloadC2SPacket(new MetadataPayload(METADATA)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void handleDisable(MinecraftClient client, ClientCommonNetworkHandler handler, DisableS2CPayload payload) {
        clear(handler);
    }

    public static void sendHandshake(ClientCommonNetworkHandler handler) {
        handler.sendPacket(new CustomPayloadC2SPacket(new HandshakePayload(CommonImpl.VERSION, ServerPackets.REGISTRY)));
    }
}
