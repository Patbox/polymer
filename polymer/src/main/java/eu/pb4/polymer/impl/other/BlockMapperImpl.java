package eu.pb4.polymer.impl.other;

import eu.pb4.polymer.api.block.BlockMapper;
import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
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
        public Block toClientSideBlock(Block block, ServerPlayerEntity player) {
            return block instanceof PolymerBlock polymerBlock ? PolymerBlockUtils.getBlockSafely(polymerBlock, block.getDefaultState(), player) : block;
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
            public Block toClientSideBlock(Block block, ServerPlayerEntity player) {
                var clientState = blockStateMap.get(block.getDefaultState());

                return clientState != null ? DEFAULT.toClientSideBlock(clientState.getBlock(), player) : Blocks.AIR;
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
            public Block toClientSideBlock(Block block, ServerPlayerEntity player) {
                return base.toClientSideBlock(overlay.toClientSideBlock(block, player), player);
            }

            @Override
            public String getMapperName() {
                return "polymer:stack [" + overlay.getMapperName() + " | " + base.getMapperName() + "]";
            }
        };
    }
}
