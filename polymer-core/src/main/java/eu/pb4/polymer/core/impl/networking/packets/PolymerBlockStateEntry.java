package eu.pb4.polymer.core.impl.networking.packets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public record PolymerBlockStateEntry(Map<String, String> states, int numId, int blockId) implements BufferWritable {
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeVarInt(numId);
        buf.writeVarInt(blockId);
        buf.writeMap(states, PacketByteBuf::writeString, PacketByteBuf::writeString);
    }

    public static PolymerBlockStateEntry of(BlockState state, ServerPlayNetworkHandler player, int version) {
        var list = new HashMap<String, String>();

        for (var entry : state.getEntries().entrySet()) {
            list.put(entry.getKey().getName(), ((Property) (Object) entry.getKey()).name(entry.getValue()));
        }

        return new PolymerBlockStateEntry(list, Block.STATE_IDS.getRawId(state), Registries.BLOCK.getRawId(state.getBlock()));
    }

    public static PolymerBlockStateEntry read(PacketByteBuf buf, int version) {
        if (version == 1) {
            var numId = buf.readVarInt();
            var blockId = buf.readVarInt();
            var states = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString);
            return new PolymerBlockStateEntry(states, numId, blockId);
        }

        return null;
    }
}
