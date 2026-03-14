package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class TestBarsBlock extends IronBarsBlock implements PolymerTexturedBlock {
    private final IdentityHashMap<BlockState, BlockState> map = new IdentityHashMap<>();
    protected TestBarsBlock(Properties settings) {
        super(settings);

        for (var state : this.getStateDefinition().getPossibleStates()) {
            var model = MultiPolymerBlockModel.of()
                    .with(state.getValue(WATERLOGGED) ? Identifier.withDefaultNamespace("block/spruce_fence_post") : Identifier.withDefaultNamespace("block/oak_fence_post"));

            var side = state.getValue(WATERLOGGED) ? Identifier.withDefaultNamespace("block/spruce_fence_side") : Identifier.withDefaultNamespace("block/oak_fence_side");
            var dirs = new ArrayList<Direction>();
            if (state.getValue(NORTH)) {
                model.with(PolymerBlockModel.of(side, 0, 0, true, 1));
                dirs.add(Direction.NORTH);
            }
            if (state.getValue(EAST)) {
                model.with(PolymerBlockModel.of(side, 0, 90, true, 1));
                dirs.add(Direction.EAST);
            }
            if (state.getValue(SOUTH)) {
                model.with(PolymerBlockModel.of(side, 0, 180, true, 1));
                dirs.add(Direction.SOUTH);
            }
            if (state.getValue(WEST)) {
                model.with(PolymerBlockModel.of(side, 0, 270, true, 1));
                dirs.add(Direction.WEST);
            }
            this.map.put(state, PolymerBlockResourceUtils.requestBlock(BlockModelType.getBars(state.getValue(WATERLOGGED), dirs), model));
        }
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.map.get(state);
    }
}
