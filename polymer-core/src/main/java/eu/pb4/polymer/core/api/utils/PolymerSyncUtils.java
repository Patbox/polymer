package eu.pb4.polymer.core.api.utils;

import eu.pb4.polymer.common.impl.EventImplUtils;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.PolymerCreativeTabApplyUpdateS2CPayload;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.CreativeModeTab;

public final class PolymerSyncUtils {

    private PolymerSyncUtils() {
    }
    /**
     * This event is run before Polymer registry sync
     */
    public static final Event<Consumer<ServerGamePacketListenerImpl>> ON_SYNC_STARTED = EventImplUtils.createConsumerEvent();
    /**
     * This event is run when it's suggested to sync custom content
     */
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> ON_SYNC_CUSTOM = EventImplUtils.createBiConsumerEvent();
    /**
     * This event is run after Polymer registry sync
     */
    public static final Event<Consumer<ServerGamePacketListenerImpl>> ON_SYNC_FINISHED = EventImplUtils.createConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> BEFORE_BLOCK_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> AFTER_BLOCK_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> BEFORE_BLOCK_STATE_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> AFTER_BLOCK_STATE_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> BEFORE_ITEM_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> AFTER_ITEM_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> BEFORE_ITEM_GROUP_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> AFTER_ITEM_GROUP_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> BEFORE_ENTITY_SYNC = EventImplUtils.createBiConsumerEvent();
    public static final Event<BiConsumer<ServerGamePacketListenerImpl, Boolean>> AFTER_ENTITY_SYNC = EventImplUtils.createBiConsumerEvent();

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizePolymerRegistries(ServerGamePacketListenerImpl handler) {
        PolymerServerProtocol.sendSyncPackets(handler, true);
    }

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizeCreativeTabs(ServerGamePacketListenerImpl handler) {
        PolymerServerProtocol.sendCreativeSyncPackets(handler);
    }

    /**
     * Sends/Updates Creative tab for player
     */
    public static void sendCreativeTab(CreativeModeTab group, ServerGamePacketListenerImpl handler) {
        PolymerServerProtocol.removeItemGroup(group, handler);
        PolymerServerProtocol.syncItemGroup(group, handler);
    }

    /**
     * Removes creative tab from player
     */
    public static void removeCreativeTab(CreativeModeTab group, ServerGamePacketListenerImpl handler) {
        PolymerServerProtocol.removeItemGroup(group, handler);
    }

    /**
     * Rebuild creative search index
     */
    public static void rebuildCreativeModeTabs(ServerGamePacketListenerImpl handler) {
        var ver = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_CREATIVE_TAB_APPLY_UPDATE);
        if (ver > -1) {
            handler.send(new ClientboundCustomPayloadPacket(new PolymerCreativeTabApplyUpdateS2CPayload()));
        }
    }

}
