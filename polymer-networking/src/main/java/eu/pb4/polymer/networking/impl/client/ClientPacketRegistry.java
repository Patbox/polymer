package eu.pb4.polymer.networking.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonPacketListenerImplExt;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import eu.pb4.polymer.networking.api.client.PolymerClientPacketHandler;
import eu.pb4.polymer.networking.impl.*;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import eu.pb4.polymer.networking.impl.packets.HelloS2CPayload;
import eu.pb4.polymer.networking.impl.packets.MetadataPayload;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

@ApiStatus.Internal
public class ClientPacketRegistry {
    public static final HashMap<Class<?>, List<PolymerClientPacketHandler<ClientCommonPacketListenerImpl, ?>>> COMMON_PACKET_LISTENERS = new HashMap<>();
    public static final HashMap<Class<?>, List<PolymerClientPacketHandler<ClientPacketListener, ?>>> PLAY_PACKET_LISTENERS = new HashMap<>();
    public static final HashMap<Class<?>, List<PolymerClientPacketHandler<ClientConfigurationPacketListenerImpl, ?>>> CONFIG_PACKET_LISTENERS = new HashMap<>();
    public static final Object2IntMap<Identifier> CLIENT_PROTOCOL = new Object2IntOpenHashMap<>();
    public static final Map<Identifier, Tag> SERVER_METADATA = new HashMap<>();
    public static final Map<Identifier, Tag> METADATA = new HashMap<>();
    public static String lastVersion;
    public static void register() {
        PolymerClientNetworking.registerCommonHandler(HandshakePayload.class, ClientPacketRegistry::handleHandshake);
        PolymerClientNetworking.registerCommonHandler(MetadataPayload.class, ClientPacketRegistry::handleMetadata);
        PolymerClientNetworking.registerCommonHandler(DisableS2CPayload.class, ClientPacketRegistry::handleDisable);
        PolymerClientNetworking.registerCommonHandler(HelloS2CPayload.class, ClientPacketRegistry::handleHello);
    }

    private static void handleHello(Minecraft client, ClientCommonPacketListenerImpl handler, HelloS2CPayload payload) {
        sendHandshake(handler);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "UnstableApiUsage"})
    public static boolean handle(Minecraft client, ClientCommonPacketListenerImpl handler, CustomPacketPayload packet) {
        var packetHandlers = COMMON_PACKET_LISTENERS.get(packet.getClass());
        boolean handled = false;
        if (packetHandlers != null) {
            for (var pHandler : packetHandlers) {
                ((PolymerClientPacketHandler) pHandler).onPacket(client, handler, packet);
            }
            handled = !packetHandlers.isEmpty();
        }

        if (handler instanceof ClientPacketListener playNetworkHandler) {
            var packetHandlers2 = PLAY_PACKET_LISTENERS.get(packet.getClass());
            if (packetHandlers2 != null) {
                for (var pHandler : packetHandlers2) {
                    ((PolymerClientPacketHandler) pHandler).onPacket(client, playNetworkHandler, packet);
                }
                handled = handled || !packetHandlers2.isEmpty();
            }
        } else if (handler instanceof ClientConfigurationPacketListenerImpl networkHandler) {
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

    public static void clear(@Nullable ClientCommonPacketListenerImpl handler) {
        lastVersion = "";
        CLIENT_PROTOCOL.clear();
        synchronized (SERVER_METADATA) {
            SERVER_METADATA.clear();
        }
        if (handler != null) {
            var ext = (ExtConnection) ((CommonPacketListenerImplExt) handler).polymerCommon$getConnection();
            ext.polymerNet$getMetadataMap().clear();
            ext.polymerNet$getSupportMap().clear();
            ext.polymerNet$setVersion("");
        }

        PolymerClientNetworking.AFTER_DISABLE.invoke(Runnable::run);
    }

    public static void handleMetadata(Minecraft client, ClientCommonPacketListenerImpl handler, MetadataPayload payload) {
        synchronized (SERVER_METADATA) {
            SERVER_METADATA.clear();
            SERVER_METADATA.putAll(payload.map());
        }

        var ext = (ExtConnection) ((CommonPacketListenerImplExt) handler).polymerCommon$getConnection();
        ext.polymerNet$getMetadataMap().clear();
        ext.polymerNet$getMetadataMap().putAll(payload.map());

        PolymerClientNetworking.AFTER_METADATA_RECEIVED.invoke(Runnable::run);
    }

    public static void handleHandshake(Minecraft client, ClientCommonPacketListenerImpl handler, HandshakePayload payload) {
        CLIENT_PROTOCOL.clear();
        SERVER_METADATA.clear();

        lastVersion = payload.version();

        payload.packetVersions().forEach((id, ver) -> CLIENT_PROTOCOL.put(id, ClientPackets.getBestSupported(id, ver)));

        var ext = (ExtConnection) ((CommonPacketListenerImplExt) handler).polymerCommon$getConnection();
        ext.polymerNet$getSupportMap().putAll(CLIENT_PROTOCOL);
        ext.polymerNet$setVersion(lastVersion);

        PolymerClientNetworking.AFTER_HANDSHAKE_RECEIVED.invoke(Runnable::run);
        sendMetadata(handler);
    }

    private static void sendMetadata(ClientCommonPacketListenerImpl handler) {
        try {
            PolymerClientNetworking.BEFORE_METADATA_SYNC.invoke(Runnable::run);
            handler.send(new ServerboundCustomPayloadPacket(new MetadataPayload(METADATA)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void handleDisable(Minecraft client, ClientCommonPacketListenerImpl handler, DisableS2CPayload payload) {
        clear(handler);
    }

    public static void sendHandshake(ClientCommonPacketListenerImpl handler) {
        handler.send(new ServerboundCustomPayloadPacket(new HandshakePayload(CommonImpl.VERSION, ServerPackets.VERSION_REGISTRY)));
    }
}
