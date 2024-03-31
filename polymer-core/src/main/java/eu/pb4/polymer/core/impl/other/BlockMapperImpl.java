package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

public class BlockMapperImpl {
    public static final BlockMapper DEFAULT = new BlockMapper() {
        @Override
        public BlockState toClientSideState(BlockState state, ServerPlayerEntity player) {
            return state.getBlock() instanceof PolymerBlock polymerBlock ? PolymerBlockUtils.getBlockStateSafely(polymerBlock, state, player) : state;
        }

        @Override
        public String getMapperName() {
            return "polymer:default";
        }
    };

    public static BlockMapper getMap(Map<BlockState, BlockState> blockStateMap) {
        return new BlockMapper() {
            @Override
            public BlockState toClientSideState(BlockState state, ServerPlayerEntity player) {
                var clientState = blockStateMap.get(state);
                return clientState != null ? DEFAULT.toClientSideState(clientState, player) : Blocks.AIR.getDefaultState();
            }

            @Override
            public String getMapperName() {
                return "polymer:from_map";
            }
        };
    }

    public static BlockMapper createStack(BlockMapper overlay, BlockMapper base) {
        return new BlockMapper() {
            @Override
            public BlockState toClientSideState(BlockState state, ServerPlayerEntity player) {
                return base.toClientSideState(overlay.toClientSideState(state, player), player);
            }

            @Override
            public String getMapperName() {
                return "polymer:stack [" + overlay.getMapperName() + " | " + base.getMapperName() + "]";
            }
        };
    }
}
