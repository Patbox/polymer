package eu.pb4.polymertest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class TestFluid extends BaseTestFluid {
    @Override
    public Fluid getSource() {
        return TestMod.STILL_FLUID;
    }

    @Override
    public Fluid getFlowing() {
        return TestMod.FLOWING_FLUID;
    }

    @Override
    public Item getBucket() {
        return TestMod.FLUID_BUCKET;
    }

    @Override
    protected int getSlopeFindDistance(LevelReader world) {
        return 8;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return TestMod.FLUID_BLOCK.defaultBlockState().setValue(BlockStateProperties.LEVEL, getLegacyLevel(fluidState));
    }

    public static class Flowing extends TestFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        protected boolean canConvertToSource(ServerLevel world) {
            return false;
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends TestFluid {

        @Override
        protected boolean canConvertToSource(ServerLevel world) {
            return false;
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }
}
