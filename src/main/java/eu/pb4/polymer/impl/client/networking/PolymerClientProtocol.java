package eu.pb4.polymer.impl.client.networking;

import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.networking.ClientPackets;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

import static eu.pb4.polymer.impl.networking.PacketUtils.buf;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PolymerClientProtocol {

    public static void sendHandshake(ClientPlayNetworkHandler handler) {
        var buf = buf(0);

        buf.writeString(PolymerMod.VERSION);
        buf.writeVarInt(ServerPackets.REGISTRY.size());

        for (var id : ServerPackets.REGISTRY.ids()) {
            buf.writeIdentifier(id);

            var entry = ServerPackets.REGISTRY.get(id);

            buf.writeVarInt(entry.length);

            for (int i : entry) {
                buf.writeVarInt(i);
            }
        }

        handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.HANDSHAKE_ID, buf));
    }

    public static void sendSyncRequest(ClientPlayNetworkHandler handler) {
        if (InternalClientRegistry.ENABLED) {
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.SYNC_REQUEST_ID, buf(0)));
        }
    }

    public static void sendPickBlock(ClientPlayNetworkHandler handler, BlockPos pos) {
        if (InternalClientRegistry.getProtocol(ClientPackets.WORLD_PICK_BLOCK_ID) == 0) {
            var buf = buf(0);
            buf.writeBlockPos(pos);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.WORLD_PICK_BLOCK_ID, buf));
        }
    }

    public static void sendPickEntity(ClientPlayNetworkHandler handler, int id) {
        if (InternalClientRegistry.getProtocol(ClientPackets.WORLD_PICK_ENTITY_ID) == 0) {
            var buf = buf(0);
            buf.writeVarInt(id);
            buf.writeBoolean(Screen.hasControlDown());
            handler.sendPacket(new CustomPayloadC2SPacket(ClientPackets.WORLD_PICK_ENTITY_ID, buf));
        }
    }
}
