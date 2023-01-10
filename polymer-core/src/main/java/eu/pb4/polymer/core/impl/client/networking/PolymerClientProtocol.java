package eu.pb4.polymer.core.impl.client.networking;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.networking.ClientPackets;
import eu.pb4.polymer.core.impl.other.EventRunners;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.networking.api.PolymerServerNetworking.buf;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolymerClientProtocol {


    public static void sendSyncRequest(ClientPlayNetworkHandler handler) {
        if (InternalClientRegistry.enabled) {
            InternalClientRegistry.delayAction(ClientPackets.SYNC_REQUEST.toString(), 200, () -> {
                InternalClientRegistry.syncRequests++;
                InternalClientRegistry.syncRequestsPostGameJoin++;
                PolymerClientUtils.ON_SYNC_REQUEST.invoke(EventRunners.RUN);
                handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.SYNC_REQUEST, buf(0)));
            });
        }
    }

    public static void sendPickBlock(ClientPlayNetworkHandler handler, BlockPos pos) {
        if (InternalClientRegistry.getClientProtocolVer(ClientPackets.WORLD_PICK_BLOCK) == 0) {
            var buf = buf(0);
            buf.writeBlockPos(pos);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.WORLD_PICK_BLOCK, buf));
        }
    }

    public static void sendTooltipContext(ClientPlayNetworkHandler handler) {
        if (InternalClientRegistry.getClientProtocolVer(ClientPackets.CHANGE_TOOLTIP) == 0) {
            InternalClientRegistry.delayAction(ClientPackets.CHANGE_TOOLTIP.toString(), 200, () -> {
                var buf = buf(0);
                buf.writeBoolean(MinecraftClient.getInstance().options.advancedItemTooltips);
                handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.CHANGE_TOOLTIP, buf));
            });
        }
    }

    public static void sendPickEntity(ClientPlayNetworkHandler handler, int id) {
        if (InternalClientRegistry.getClientProtocolVer(ClientPackets.WORLD_PICK_ENTITY) == 0) {
            var buf = buf(0);
            buf.writeVarInt(id);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.WORLD_PICK_ENTITY, buf));
        }
    }
}
