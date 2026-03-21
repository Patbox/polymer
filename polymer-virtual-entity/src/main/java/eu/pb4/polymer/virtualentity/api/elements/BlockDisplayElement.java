package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDisplayElement extends DisplayElement {
    public BlockDisplayElement(BlockState state) {
        this.setBlockState(state);
    }

    public BlockDisplayElement() {}

    public void setBlockState(BlockState state) {
        this.syncedData.set(DisplayEntityData.Block.BLOCK_STATE, state);
    }

    public BlockState getBlockState() {
        return this.syncedData.get(DisplayEntityData.Block.BLOCK_STATE);
    }

    @Override
    protected final EntityType<? extends Display> getEntityType() {
        return EntityType.BLOCK_DISPLAY;
    }
}
