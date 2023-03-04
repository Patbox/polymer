package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;

public class BlockDisplayElement extends DisplayElement {
    public BlockDisplayElement(BlockState state) {
        this.setBlockState(state);
    }

    public BlockDisplayElement() {}

    public void setBlockState(BlockState state) {
        this.dataTracker.set(DisplayTrackedData.Block.BLOCK_STATE, state);
    }

    public BlockState getBlockState() {
        return this.dataTracker.get(DisplayTrackedData.Block.BLOCK_STATE);
    }

    @Override
    protected final EntityType<? extends DisplayEntity> getEntityType() {
        return EntityType.BLOCK_DISPLAY;
    }
}
