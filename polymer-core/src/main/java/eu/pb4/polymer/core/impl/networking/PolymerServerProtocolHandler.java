package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.interfaces.PolymerGamePacketListenerExtension;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerChangeTooltipC2SPayload;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerServerProtocolHandler {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        PolymerServerNetworking.registerPlayHandler(PolymerChangeTooltipC2SPayload.class, PolymerServerProtocolHandler::handleTooltipChange);

        PolymerServerNetworking.ON_PLAY_SYNC.register((handler, x) -> {
            PolymerServerProtocol.sendSyncPackets(handler, true);
        });

        ServerMetadataKeys.setup();
        S2CPackets.SYNC_BLOCK.getNamespace();
        C2SPackets.CHANGE_TOOLTIP.getNamespace();
    }

    private static void handleTooltipChange(MinecraftServer server, ServerGamePacketListenerImpl handler, PolymerChangeTooltipC2SPayload payload) {
        handler.getPlayer().level().getServer().execute(() -> {
            PolymerGamePacketListenerExtension.of(handler).polymer$setAdvancedTooltip(payload.advanced());

            if (PolymerServerNetworking.getLastPacketReceivedTime(handler, C2SPackets.CHANGE_TOOLTIP) + 1000 < System.currentTimeMillis()) {
                PolymerSyncUtils.synchronizeCreativeTabs(handler);
                PolymerUtils.reloadInventory(handler.player);
            }
        });
    }
}
