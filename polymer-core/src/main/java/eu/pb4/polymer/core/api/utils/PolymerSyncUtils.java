package eu.pb4.polymer.core.api.utils;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PolymerSyncUtils {

    private PolymerSyncUtils() {
    }
    /**
     * This event is run before Polymer registry sync
     */
    public static final SimpleEvent<Consumer<ServerPlayNetworkHandler>> ON_SYNC_STARTED = new SimpleEvent<>();
    /**
     * This event is run when it's suggested to sync custom content
     */
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> ON_SYNC_CUSTOM = new SimpleEvent<>();
    /**
     * This event is run after Polymer registry sync
     */
    public static final SimpleEvent<Consumer<ServerPlayNetworkHandler>> ON_SYNC_FINISHED = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> BEFORE_BLOCK_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> AFTER_BLOCK_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> BEFORE_BLOCK_STATE_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> AFTER_BLOCK_STATE_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> BEFORE_ITEM_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> AFTER_ITEM_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> BEFORE_ITEM_GROUP_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> AFTER_ITEM_GROUP_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> BEFORE_ENTITY_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<BiConsumer<ServerPlayNetworkHandler, Boolean>> AFTER_ENTITY_SYNC = new SimpleEvent<>();

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizePolymerRegistries(ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.sendSyncPackets(handler, true);
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
    public static void sendCreativeTab(ItemGroup group, ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.removeItemGroup(group, handler);
        PolymerServerProtocol.syncItemGroup(group, handler);
    }

    /**
     * Removes creative tab from player
     */
    public static void removeCreativeTab(ItemGroup group, ServerPlayNetworkHandler handler) {
        PolymerServerProtocol.removeItemGroup(group, handler);
        PolymerServerProtocol.removeItemGroup(group, handler);
    }

    /**
     * Rebuild creative search index
     */
    public static void rebuildItemGroups(ServerPlayNetworkHandler handler) {
        var ver = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_APPLY_UPDATE);
        if (ver > -1) {
            //handler.sendPacket(new CustomPayloadS2CPacketMixin(S2CPackets.SYNC_ITEM_GROUP_APPLY_UPDATE, PolymerServerNetworking.buf(ver)));
        }
    }

}
