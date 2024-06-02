package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerChangeTooltipC2SPayload;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerPickBlockC2SPayload;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerPickEntityC2SPayload;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerServerProtocolHandler {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        PolymerServerNetworking.registerPlayHandler(PolymerPickBlockC2SPayload.class, PolymerServerProtocolHandler::handlePickBlock);
        PolymerServerNetworking.registerPlayHandler(PolymerPickEntityC2SPayload.class, PolymerServerProtocolHandler::handlePickEntity);
        PolymerServerNetworking.registerPlayHandler(PolymerChangeTooltipC2SPayload.class, PolymerServerProtocolHandler::handleTooltipChange);

        PolymerServerNetworking.ON_PLAY_SYNC.register((handler, x) -> {
            PolymerServerProtocol.sendSyncPackets(handler, true);
        });

        ServerMetadataKeys.setup();
        S2CPackets.SYNC_BLOCK.getNamespace();
        C2SPackets.WORLD_PICK_BLOCK.getNamespace();
    }

    private static void handleTooltipChange(MinecraftServer server, ServerPlayNetworkHandler handler, PolymerChangeTooltipC2SPayload payload) {
        handler.getPlayer().getServer().execute(() -> {
            PolymerPlayNetworkHandlerExtension.of(handler).polymer$setAdvancedTooltip(payload.advanced());

            if (PolymerServerNetworking.getLastPacketReceivedTime(handler, C2SPackets.CHANGE_TOOLTIP) + 1000 < System.currentTimeMillis()) {
                PolymerSyncUtils.synchronizeCreativeTabs(handler);
                PolymerUtils.reloadInventory(handler.player);
            }
        });
    }

    private static void handlePickBlock(MinecraftServer server, ServerPlayNetworkHandler handler, PolymerPickBlockC2SPayload payload) {
        var pos = payload.pos();
        var ctr = payload.control();


        server.execute(() -> {
            if (pos.getManhattanDistance(handler.player.getBlockPos()) <= 32) {
                PolymerImplUtils.pickBlock(handler.player, pos, ctr);
            }
        });
    }

    private static void handlePickEntity(MinecraftServer server, ServerPlayNetworkHandler handler, PolymerPickEntityC2SPayload payload) {
        server.execute(() -> {
            var entity = handler.player.getServerWorld().getEntityById(payload.entityId());

            if (entity != null && entity.getPos().relativize(handler.player.getPos()).lengthSquared() < 1024) {
                PolymerImplUtils.pickEntity(handler.player, entity);
            }
        });
    }
}
