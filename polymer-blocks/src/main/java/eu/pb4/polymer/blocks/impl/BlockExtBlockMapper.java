package eu.pb4.polymer.blocks.impl;

import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public class BlockExtBlockMapper implements BlockMapper {
    public static final BlockExtBlockMapper INSTANCE = new BlockExtBlockMapper(BlockMapper.createDefault());

    public final Map<BlockState, BlockState> stateMap = new IdentityHashMap<>();
    private final BlockMapper baseMapper;

    public BlockExtBlockMapper(BlockMapper baseMapper) {
        this.baseMapper = baseMapper;
    }

    @Override
    public BlockState toClientSideState(BlockState state, PacketContext player) {
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerTexturedBlock) {
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
