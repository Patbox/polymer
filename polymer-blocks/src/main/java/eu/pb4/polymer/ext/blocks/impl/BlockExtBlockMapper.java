package eu.pb4.polymer.ext.blocks.impl;

import eu.pb4.polymer.api.block.BlockMapper;
import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class BlockExtBlockMapper implements BlockMapper {
    public static final BlockExtBlockMapper INSTANCE = new BlockExtBlockMapper(BlockMapper.createDefault());

    public final Map<BlockState, BlockState> stateMap = new HashMap<>();
    private final BlockMapper baseMapper;

    public BlockExtBlockMapper(BlockMapper baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public BlockState toClientSideState(BlockState state, ServerPlayerEntity player) {
        if (state.getBlock() instanceof PolymerTexturedBlock) {
            return this.baseMapper.toClientSideState(state, player);
        }

        var parsedState = BlockMapper.createDefault().toClientSideState(state, player);
        return stateMap.getOrDefault(parsedState, parsedState);
    }

    @Override
    public Block toClientSideBlock(Block block, ServerPlayerEntity player) {
        return this.baseMapper.toClientSideBlock(block, player);
    }

    @Override
    public String getMapperName() {
        return "polymer:textured_blocks";
    }
}
