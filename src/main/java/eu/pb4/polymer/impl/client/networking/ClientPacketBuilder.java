package eu.pb4.polymer.impl.client.networking;

import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.networking.PolymerPacketIds;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientPacketBuilder {
    public static PacketByteBuf buf() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void sendVersion(ClientPlayNetworkHandler handler) {
        var buf = buf();

        buf.writeShort(PolymerMod.PROTOCOL_VERSION);
        buf.writeString(PolymerMod.VERSION);

        handler.sendPacket(new CustomPayloadC2SPacket(PolymerPacketIds.VERSION_ID, buf));
    }

    public static void sendPickBlock(ClientPlayNetworkHandler handler, BlockPos pos) {
        if (InternalClientRegistry.ENABLED) {
            var buf = buf();
            buf.writeBlockPos(pos);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(PolymerPacketIds.PICK_BLOCK_ID, buf));
        }
    }
}
