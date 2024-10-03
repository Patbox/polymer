package eu.pb4.polymer.blocks.impl;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.BlockMapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class BlockExtBlockMapper implements BlockMapper {
    public static final BlockExtBlockMapper INSTANCE = new BlockExtBlockMapper(BlockMapper.createDefault());

    public final Map<BlockState, BlockState> stateMap = new IdentityHashMap<>();
    private final BlockMapper baseMapper;

    public BlockExtBlockMapper(BlockMapper baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public BlockState toClientSideState(BlockState state, PacketContext player) {
        if (state.getBlock() instanceof PolymerTexturedBlock) {
            return this.baseMapper.toClientSideState(state, player);
        }

        var parsedState = this.baseMapper.toClientSideState(state, player);
        return stateMap.getOrDefault(parsedState, parsedState);
    }

    @Override
    public String getMapperName() {
        return "polymer:textured_blocks";
    }
}
