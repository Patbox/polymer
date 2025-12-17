package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMapperImpl {
    public static final BlockMapper DEFAULT = new BlockMapper() {
        @Override
        public BlockState toClientSideState(BlockState state, PacketContext player) {
            return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock polymerBlock ? PolymerBlockUtils.getBlockStateSafely(polymerBlock, state, player) : state;
        }

        @Override
        public String getMapperName() {
            return "polymer:default";
        }
    };

    public static BlockMapper getMap(Map<BlockState, BlockState> blockStateMap) {
        return new BlockMapper() {
            @Override
            public BlockState toClientSideState(BlockState state, PacketContext player) {
                var clientState = blockStateMap.get(state);
                return clientState != null ? DEFAULT.toClientSideState(clientState, player) : Blocks.AIR.defaultBlockState();
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
            public BlockState toClientSideState(BlockState state, PacketContext player) {
                return base.toClientSideState(overlay.toClientSideState(state, player), player);
            }

            @Override
            public String getMapperName() {
                return "polymer:stack [" + overlay.getMapperName() + " | " + base.getMapperName() + "]";
            }
        };
    }
}
