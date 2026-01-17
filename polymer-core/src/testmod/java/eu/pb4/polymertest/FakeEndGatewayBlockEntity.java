package eu.pb4.polymertest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FakeEndGatewayBlockEntity extends BlockEntity {
    private int value = 0;

    public FakeEndGatewayBlockEntity(BlockPos pos, BlockState blockState) {
        super(TestMod.END_GATEWAY_BE, pos, blockState);
    }


    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.value = input.getIntOr("value", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("value", this.value);
    }
}
