package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@ApiStatus.Internal
public record DebugBlockStateEntry(Map<String, String> states, int numId, Identifier blockId) {
    public static final StreamCodec<ContextByteBuf, DebugBlockStateEntry> CODEC = StreamCodec.ofMember(DebugBlockStateEntry::write, DebugBlockStateEntry::read);


    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(numId);
        buf.writeIdentifier(blockId);
        buf.writeMap(states, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    public static DebugBlockStateEntry of(BlockState state, ServerGamePacketListenerImpl player, int version) {
        var list = new HashMap<String, String>();

        for (var entry : state.getValues().entrySet()) {
            list.put(entry.getKey().getName(), ((Property) entry.getKey()).getName(entry.getValue()));
        }

        return new DebugBlockStateEntry(list,
                Block.BLOCK_STATE_REGISTRY.getId(state),
                BuiltInRegistries.BLOCK.getKey(state.getBlock())
        );
    }

    public static DebugBlockStateEntry read(FriendlyByteBuf buf) {
        var numId = buf.readVarInt();
        var blockId = buf.readIdentifier();
        var states = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
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
