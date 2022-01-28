package eu.pb4.polymer.ext.blocks.impl;

import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.impl.other.BlockMapperImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class BlockExtBlockMapper implements BlockMapper {
    public static final BlockExtBlockMapper INSTANCE = new BlockExtBlockMapper();

    public final Map<BlockState, BlockState> stateMap = new HashMap<>();

    @Override
    public BlockState toClientSideState(BlockState state, ServerPlayerEntity player) {
        if (state.getBlock() instanceof PolymerTexturedBlock) {
            return BlockMapperImpl.DEFAULT.toClientSideState(state, player);
        }

        var parsedState = BlockMapperImpl.DEFAULT.toClientSideState(state, player);
        return stateMap.getOrDefault(parsedState, parsedState);
    }

    @Override
    public Block toClientSideBlock(Block block, ServerPlayerEntity player) {
        return BlockMapperImpl.DEFAULT.toClientSideBlock(block, player);
    }

    @Override
    public String getMapperName() {
        return "polymer:textured_blocks";
    }
}
