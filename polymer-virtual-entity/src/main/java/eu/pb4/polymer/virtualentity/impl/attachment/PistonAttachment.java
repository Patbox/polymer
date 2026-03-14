package eu.pb4.polymer.virtualentity.impl.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;


public final class PistonAttachment extends ChunkAttachment implements BlockAwareAttachment {
    private final BlockPos blockPos;
    private final Direction direction;
    private BlockState blockState;

    public PistonAttachment(ElementHolder holder, LevelChunk chunk, BlockState state, BlockPos blockPos, Direction direction) {
        super(holder, chunk, Vec3.atCenterOf(blockPos), false);
        this.blockPos = blockPos;
        this.direction = direction;
        this.blockState = state;
        this.attach();
    }

    @Override
    protected void attach() {
        if (this.blockPos != null) {
            super.attach();
        }
    }

    @Override
    public boolean canUpdatePosition() {
        return true;
    }

    public void update(float d) {
        this.pos = Vec3.atCenterOf(this.blockPos).relative(this.direction, d);
        this.holder().tick();
    }
    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @ApiStatus.Internal
    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
        if (this == this.holder().getAttachment()) {
            this.holder().notifyUpdate(BLOCK_STATE_UPDATE);
        }
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean isPartOfTheWorld() {
        return true;
    }
}
