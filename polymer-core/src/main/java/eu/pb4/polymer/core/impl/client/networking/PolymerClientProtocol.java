package eu.pb4.polymer.core.impl.client.networking;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.networking.C2SPackets;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerChangeTooltipC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolymerClientProtocol {
    public static void sendTooltipContext(ClientPacketListener handler) {
        if (InternalClientRegistry.getClientProtocolVer(C2SPackets.CHANGE_TOOLTIP) != -1) {
            InternalClientRegistry.delayAction(C2SPackets.CHANGE_TOOLTIP.toString(), 200, () -> {
                handler.send(new ServerboundCustomPayloadPacket(new PolymerChangeTooltipC2SPayload(Minecraft.getInstance().options.advancedItemTooltips)));
            });
        }
    }
}
