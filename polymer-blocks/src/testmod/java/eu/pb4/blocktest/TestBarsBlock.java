package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.IdentityHashMap;

public class TestBarsBlock extends PaneBlock implements PolymerTexturedBlock {
    private final IdentityHashMap<BlockState, BlockState> map = new IdentityHashMap<>();
    protected TestBarsBlock(Settings settings) {
        super(settings);

        for (var state : this.getStateManager().getStates()) {
            var model = MultiPolymerBlockModel.of()
                    .with(state.get(WATERLOGGED) ? Identifier.ofVanilla("block/spruce_fence_post") : Identifier.ofVanilla("block/oak_fence_post"));

            var side = state.get(WATERLOGGED) ? Identifier.ofVanilla("block/spruce_fence_side") : Identifier.ofVanilla("block/oak_fence_side");
            var dirs = new ArrayList<Direction>();
            if (state.get(NORTH)) {
                model.with(PolymerBlockModel.of(side, 0, 0, true, 1));
                dirs.add(Direction.NORTH);
            }
            if (state.get(EAST)) {
                model.with(PolymerBlockModel.of(side, 0, 90, true, 1));
                dirs.add(Direction.EAST);
            }
            if (state.get(SOUTH)) {
                model.with(PolymerBlockModel.of(side, 0, 180, true, 1));
                dirs.add(Direction.SOUTH);
            }
            if (state.get(WEST)) {
                model.with(PolymerBlockModel.of(side, 0, 270, true, 1));
                dirs.add(Direction.WEST);
            }
            this.map.put(state, PolymerBlockResourceUtils.requestBlock(BlockModelType.getBars(state.get(WATERLOGGED), dirs), model));
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.map.get(state);
    }
}
