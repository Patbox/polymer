package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public record PolymerBlockStateEntry(Map<String, String> states, int numId, int blockId) implements BufferWritable {
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeVarInt(numId);
        buf.writeVarInt(blockId);
        buf.writeMap(states, (buf2, string) -> buf2.writeString(string), (buf2, string) -> buf2.writeString(string));
    }

    public static PolymerBlockStateEntry of(BlockState state, ServerPlayNetworkHandler player) {
        var list = new HashMap<String, String>();

        for (var entry : state.getEntries().entrySet()) {
            list.put(entry.getKey().getName(), entry.getValue().toString());
        }

        return new PolymerBlockStateEntry(list, PolymerServerProtocol.getRawId(state, player.player), Registry.BLOCK.getRawId(state.getBlock()));
    }

    public static PolymerBlockStateEntry read(PacketByteBuf buf, int version) {
        if (version == 0) {
            var numId = buf.readVarInt();
            var blockId = buf.readVarInt();
            var states = buf.readMap((buf2) -> buf.readString(), (buf2) -> buf.readString());
            return new PolymerBlockStateEntry(states, numId, blockId);
        }

        return null;
    }
}
