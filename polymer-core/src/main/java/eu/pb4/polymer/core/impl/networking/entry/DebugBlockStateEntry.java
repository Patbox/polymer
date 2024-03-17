package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public record DebugBlockStateEntry(Map<String, String> states, int numId, Identifier blockId) {
    public static final PacketCodec<ContextByteBuf, DebugBlockStateEntry> CODEC = PacketCodec.of(DebugBlockStateEntry::write, DebugBlockStateEntry::read);


    public void write(PacketByteBuf buf) {
        buf.writeVarInt(numId);
        buf.writeIdentifier(blockId);
        buf.writeMap(states, PacketByteBuf::writeString, PacketByteBuf::writeString);
    }

    public static DebugBlockStateEntry of(BlockState state, ServerPlayNetworkHandler player, int version) {
        var list = new HashMap<String, String>();

        for (var entry : state.getEntries().entrySet()) {
            list.put(entry.getKey().getName(), ((Property) entry.getKey()).name(entry.getValue()));
        }

        return new DebugBlockStateEntry(list,
                Block.STATE_IDS.getRawId(state),
                Registries.BLOCK.getId(state.getBlock())
        );
    }

    public static DebugBlockStateEntry read(PacketByteBuf buf) {
        var numId = buf.readVarInt();
        var blockId = buf.readIdentifier();
        var states = buf.readMap(PacketByteBuf::readString, PacketByteBuf::readString);
        return new DebugBlockStateEntry(states, numId, blockId);
    }

    public String asString() {
        var builder = new StringBuilder();

        builder.append(this.blockId);

        if (!this.states.isEmpty()) {
            builder.append("[");
            var iterator = this.states().entrySet().stream().sorted().iterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                builder.append(entry.getKey());
                builder.append("=");
                builder.append(entry.getValue());

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }

        return builder.toString();
    }
}
