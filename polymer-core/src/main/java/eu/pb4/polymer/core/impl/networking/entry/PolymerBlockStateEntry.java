package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.IntFunction;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@ApiStatus.Internal
public record PolymerBlockStateEntry(Map<String, String> properties, int numId, int blockId) {
    public static final IdentityHashMap<BlockState, PolymerBlockStateEntry> CACHE = new IdentityHashMap<>();

    public static final StreamCodec<ContextByteBuf, PolymerBlockStateEntry> CODEC = StreamCodec.ofMember(PolymerBlockStateEntry::write, PolymerBlockStateEntry::read);

    private static final StreamCodec<ByteBuf, Map<String, String>> STRING_MAP_CODEC = ByteBufCodecs.map((IntFunction<Map<String, String>>) HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8);

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(numId);
        buf.writeVarInt(blockId);
        STRING_MAP_CODEC.encode(buf, properties);
    }

    public static PolymerBlockStateEntry of(BlockState state) {
        var value = CACHE.get(state);
        if (value == null) {
            var list = new HashMap<String, String>();

            for (var entry : state.getValues().toList()) {
                list.put(entry.property().getName(), entry.valueName());
            }
            value = new PolymerBlockStateEntry(list, Block.BLOCK_STATE_REGISTRY.getId(state), BuiltInRegistries.BLOCK.getId(state.getBlock()));
            CACHE.put(state, value);
        }

        return value;
    }

    public static PolymerBlockStateEntry read(FriendlyByteBuf buf) {
        var numId = buf.readVarInt();
        var blockId = buf.readVarInt();
        var states = STRING_MAP_CODEC.decode(buf);
        return new PolymerBlockStateEntry(states, numId, blockId);
    }
}
