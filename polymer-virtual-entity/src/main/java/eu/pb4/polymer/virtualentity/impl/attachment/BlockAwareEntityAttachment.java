package eu.pb4.polymer.virtualentity.impl.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import eu.pb4.polymer.virtualentity.api.LocalInterpolationHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

public class BlockAwareEntityAttachment extends EntityAttachment implements BlockAwareAttachment {
    private final LocalInterpolationHandler interpolator;
    private BlockState blockState;

    public BlockAwareEntityAttachment(ElementHolder holder, BlockState state, Entity entity) {
        super(holder, entity, true);
        this.blockState = state;
        this.interpolator = new LocalInterpolationHandler(entity);
        this.attach();
    }

    @Override
    public void tick() {
        if (this.entity.tickCount % 3 == 0) {
            this.interpolator.interpolateTo(entity.position(), entity.getYRot(), entity.getXRot());
        }
        this.interpolator.interpolate();
        super.tick();
    }

    @Override
    public BlockPos getBlockPos() {
        return BlockPos.ZERO;
    }

    @Override
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean isPartOfTheWorld() {
        return false;
    }

    @Override
    public Vec3 getPos() {
        return this.interpolator.position().add(0, 0.5, 0);
    }

    @ApiStatus.Internal
    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
        if (this == this.holder().getAttachment()) {
            this.holder().notifyUpdate(BLOCK_STATE_UPDATE);
        }
    }

    public static BlockState getBlockStateFrom(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return Blocks.AIR.defaultBlockState();
        }

        return stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).apply(blockItem.getBlock().defaultBlockState());
    }
}
