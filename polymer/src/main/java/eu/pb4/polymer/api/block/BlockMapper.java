package eu.pb4.polymer.api.block;

import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.other.BlockMapperImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Do not use, unless you really need it and you are 100% sure about what you need!
 *
 * Allows changing how blocks display for certain players.
 * You can replace any block that way, including vanilla ones.
 *
 * To only change your own blocks see {@link PolymerBlock}
 */
public interface BlockMapper {
    SimpleEvent<BiFunction<ServerPlayerEntity, BlockMapper, @Nullable BlockMapper>> DEFAULT_MAPPER_EVENT = new SimpleEvent<>();

    BlockState toClientSideState(BlockState state, ServerPlayerEntity player);
    Block toClientSideBlock(Block block, ServerPlayerEntity player);

    String getMapperName();

    static BlockMapper createDefault() {
        return BlockMapperImpl.DEFAULT;
    }

    static BlockMapper getDefault(ServerPlayerEntity player) {
        var obj = new MutableObject<>(BlockMapperImpl.DEFAULT);
        DEFAULT_MAPPER_EVENT.invoke((c) -> {
             var mapper = c.apply(player, obj.getValue());

             if (mapper != null) {
                 obj.setValue(mapper);
             }
        });

        return obj.getValue();
    }

    static BlockMapper createMap(Map<BlockState, BlockState> stateMap) {
        return BlockMapperImpl.getMap(stateMap);
    }

    static BlockMapper createStack(BlockMapper overlay, BlockMapper base) {
        return BlockMapperImpl.createStack(overlay, base);
    }

    static BlockMapper getFrom(@Nullable ServerPlayerEntity player) {
        return player != null ? PolymerNetworkHandlerExtension.of(player).polymer_getBlockMapper() : BlockMapper.createDefault();
    }

    static void resetMapper(@Nullable ServerPlayerEntity player) {
        if (player != null) {
            PolymerNetworkHandlerExtension.of(player).polymer_setBlockMapper(getDefault(player));
        }
    }

    static void set(ServerPlayNetworkHandler handler, BlockMapper mapper) {
        PolymerNetworkHandlerExtension.of(handler).polymer_setBlockMapper(mapper);
    }

    static BlockMapper get(ServerPlayNetworkHandler handler) {
        return PolymerNetworkHandlerExtension.of(handler).polymer_getBlockMapper();
    }
}
