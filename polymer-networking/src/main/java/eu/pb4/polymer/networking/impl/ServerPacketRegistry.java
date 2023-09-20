package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.networking.api.server.PolymerServerPacketHandler;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import eu.pb4.polymer.networking.impl.packets.HandshakePayload;
import eu.pb4.polymer.networking.impl.packets.HelloS2CPayload;
import eu.pb4.polymer.networking.impl.packets.MetadataPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.nbt.NbtElement;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class ServerPacketRegistry {
    public static final HashMap<Class<?>, List<PolymerServerPacketHandler<ServerCommonNetworkHandler, ?>>> COMMON_PACKET_LISTENERS = new HashMap<>();
    public static final HashMap<Class<?>, List<PolymerServerPacketHandler<ServerPlayNetworkHandler, ?>>> PLAY_PACKET_LISTENERS = new HashMap<>();
    public static final HashMap<Class<?>, List<PolymerServerPacketHandler<ServerConfigurationNetworkHandler, ?>>> CONFIG_PACKET_LISTENERS = new HashMap<>();

    public static final HashMap<Identifier, NbtElement> METADATA = new HashMap<>();
    public static void register() {
        PolymerNetworking.registerCommonPayload(HandshakePayload.ID, 2, HandshakePayload::read);
        PolymerNetworking.registerCommonPayload(MetadataPayload.ID, 2, MetadataPayload::read);
        PolymerNetworking.registerS2CPayload(DisableS2CPayload.ID, 2, DisableS2CPayload::read);
        PolymerNetworking.registerS2CPayload(HelloS2CPayload.ID, 2, HelloS2CPayload::read);


        PolymerServerNetworking.registerCommonHandler(HandshakePayload.class,
                (server, handler, packet) -> handleHandshake(PolymerHandshakeHandler.of(server, handler), packet));
        PolymerServerNetworking.registerCommonHandler(MetadataPayload.class,
                (server, handler, packet) -> handleMetadata(PolymerHandshakeHandler.of(server, handler), packet));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean handle(MinecraftServer server, ServerCommonNetworkHandler handler, CustomPayload packet) {
        var packetHandlers = COMMON_PACKET_LISTENERS.get(packet.getClass());
        boolean handled = false;
        if (packetHandlers != null) {
            for (var pHandler : packetHandlers) {
                ((PolymerServerPacketHandler) pHandler).onPacket(server, handler, packet);
            }
            handled = !packetHandlers.isEmpty();
        }

        if (handler instanceof ServerPlayNetworkHandler playNetworkHandler) {
            var packetHandlers2 = PLAY_PACKET_LISTENERS.get(packet.getClass());
            if (packetHandlers2 != null) {
                for (var pHandler : packetHandlers2) {
                    ((PolymerServerPacketHandler) pHandler).onPacket(server, playNetworkHandler, packet);
                }
                handled = handled || !packetHandlers2.isEmpty();
            }
        } else if (handler instanceof ServerConfigurationNetworkHandler networkHandler) {
            var packetHandlers2 = CONFIG_PACKET_LISTENERS.get(packet.getClass());
            if (packetHandlers2 != null) {
                for (var pHandler : packetHandlers2) {
                    ((PolymerServerPacketHandler) pHandler).onPacket(server, networkHandler, packet);
                }
                handled = handled || !packetHandlers2.isEmpty();
            }
        }

        return handled;
    }
    public static void sendHandshake(PolymerHandshakeHandler handler) {
        handler.sendPacket(new CustomPayloadS2CPacket(new HandshakePayload(CommonImpl.VERSION, ClientPackets.REGISTRY)));
    }

    private static void sendMetadata(PolymerHandshakeHandler handler) {
        handler.sendPacket(new CustomPayloadS2CPacket(new MetadataPayload(METADATA)));
    }

    public static void handleHandshake(PolymerHandshakeHandler handler, HandshakePayload payload) {
        var versionMap = new Object2IntOpenHashMap<Identifier>();

        payload.packetVersions().forEach((id, versions) -> {
            versionMap.put(id, ServerPackets.getBestSupported(id, versions));
        });

        handler.getServer().execute(() -> {
            handler.set(handler.getPolymerVersion(), versionMap);
            handler.setLastPacketTime(HandshakePayload.ID);

            sendHandshake(handler);
            sendMetadata(handler);
        });
    }

    public static void handleMetadata(PolymerHandshakeHandler handler, MetadataPayload payload) {
        payload.map().forEach(handler::setMetadataValue);
    }
}
