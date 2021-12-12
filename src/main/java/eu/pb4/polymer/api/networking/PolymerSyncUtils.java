package eu.pb4.polymer.api.networking;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.Consumer;

public final class PolymerSyncUtils {
    private PolymerSyncUtils() {
    }

    /**
     * This event is run after receiving client handshake
     */
    public static final SimpleEvent<Consumer<PolymerHandshakeHandler>> ON_HANDSHAKE = new SimpleEvent<>();
    /**
     * This event ir run before Polymer registry sync
     */
    public static final SimpleEvent<Consumer<ServerPlayNetworkHandler>> ON_SYNC_STARTED = new SimpleEvent<>();
    /**
     * This event ir run after Polymer registry sync
     */
    public static final SimpleEvent<Consumer<ServerPlayNetworkHandler>> ON_SYNC_FINISHED = new SimpleEvent<>();


    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizePolymerRegistries(ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.sendSyncPackets(handler);
    }

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizeCreativeTabs(ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.sendCreativeSyncPackets(handler);
    }

    /**
     * Sends/Updates Creative tab for player
     */
    public static void sendCreativeTab(PolymerItemGroup group, ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.removeItemGroup(group, handler);
        PolymerServerProtocol.syncItemGroup(group, handler);
    }

    /**
     * Removes creative tab from player
     */
    public static void removeCreativeTab(PolymerItemGroup group, ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.removeItemGroup(group, handler);
    }

    /**
     * Rebuild creative search index
     */
    public static void rebuildCreativeSearch(ServerPlayNetworkHandler handler) {
        if (PolymerNetworkHandlerExtension.of(handler).polymer_getSupportedVersion(ServerPackets.SYNC_REBUILD_SEARCH) == 0) {
            handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_REBUILD_SEARCH_ID, PolymerPacketUtils.buf(0)));
        }
    }
}
