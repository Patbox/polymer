package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

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
        ByteBufCodecs.map((IntFunction<Map<String, String>>) HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8)
                        .encode(buf, this.states);
    }

    public static DebugBlockStateEntry of(BlockState state, ServerGamePacketListenerImpl player, int version) {
        var list = new HashMap<String, String>();

        for (var entry : state.getValues().toList()) {
            list.put(entry.property().getName(), entry.valueName());
        }

        return new DebugBlockStateEntry(list,
                Block.BLOCK_STATE_REGISTRY.getId(state),
                BuiltInRegistries.BLOCK.getKey(state.getBlock())
        );
    }

    public static DebugBlockStateEntry read(FriendlyByteBuf buf) {
        var numId = buf.readVarInt();
        var blockId = buf.readIdentifier();
        var states = ByteBufCodecs.map((IntFunction<Map<String, String>>) HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8).decode(buf);
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
