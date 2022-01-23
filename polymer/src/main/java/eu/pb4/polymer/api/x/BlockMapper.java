package eu.pb4.polymer.api.x;

import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.other.BlockMapperImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Do not use, unless you really need it! See package-info for more information
 *
 * Allows to change how blocks display for certain players
 * You can replace any block that way, including vanilla ones
 */
public interface BlockMapper {
    BlockState toClientSideState(BlockState state, ServerPlayerEntity player);
    Block toClientSideBlock(Block block, ServerPlayerEntity player);

    String getMapperName();

    static BlockMapper createDefault() {
        return BlockMapperImpl.DEFAULT;
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
            PolymerNetworkHandlerExtension.of(player).polymer_setBlockMapper(createDefault());
        }
    }

    static void set(ServerPlayNetworkHandler handler, BlockMapper mapper) {
        PolymerNetworkHandlerExtension.of(handler).polymer_setBlockMapper(mapper);
    }

    static BlockMapper get(ServerPlayNetworkHandler handler) {
        return PolymerNetworkHandlerExtension.of(handler).polymer_getBlockMapper();
    }
}
