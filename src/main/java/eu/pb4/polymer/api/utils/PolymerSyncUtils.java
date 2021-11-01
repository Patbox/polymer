package eu.pb4.polymer.api.utils;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.impl.networking.PolymerPacketIds;
import eu.pb4.polymer.impl.networking.ServerPacketBuilders;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public final class PolymerSyncUtils {
    private PolymerSyncUtils(){};

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizePolymerRegistries(ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.createSyncPackets(handler);
    }

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizeCreativeTabs(ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.createCreativeTabSync(handler);
    }

    /**
     * Sends/Updates Creative tab for player
     */
    public static void sendCreativeTab(PolymerItemGroup group, ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.removeItemGroup(group, handler);
        ServerPacketBuilders.syncItemGroup(group, handler);
    }

    /**
     * Removes creative tab from player
     */
    public static void removeCreativeTab(PolymerItemGroup group, ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.removeItemGroup(group, handler);
    }

    /**
     * Rebuild creative search index
     */
    public static void rebuildCreativeSearch(ServerPlayNetworkHandler handler) {
        handler.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.REGISTRY_RESET_SEARCH_ID, ServerPacketBuilders.buf()));
    }
}
