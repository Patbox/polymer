package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
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

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(numId);
        buf.writeVarInt(blockId);
        buf.writeMap(properties, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    public static PolymerBlockStateEntry of(BlockState state) {
        var value = CACHE.get(state);
        if (value == null) {
            var list = new HashMap<String, String>();

            for (var entry : state.getValues().entrySet()) {
                list.put(entry.getKey().getName(), ((Property) (Object) entry.getKey()).getName(entry.getValue()));
            }
            value = new PolymerBlockStateEntry(list, Block.BLOCK_STATE_REGISTRY.getId(state), BuiltInRegistries.BLOCK.getId(state.getBlock()));
            CACHE.put(state, value);
        }

        return value;
    }

    public static PolymerBlockStateEntry read(FriendlyByteBuf buf) {
        var numId = buf.readVarInt();
        var blockId = buf.readVarInt();
        var states = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
        return new PolymerBlockStateEntry(states, numId, blockId);
    }
}
