package eu.pb4.polymer.virtualentity.impl.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;


public final class PistonAttachment extends ChunkAttachment implements BlockAwareAttachment {
    private final BlockPos blockPos;
    private final Direction direction;
    private BlockState blockState;

    public PistonAttachment(ElementHolder holder, WorldChunk chunk, BlockState state, BlockPos blockPos, Direction direction) {
        super(holder, chunk, Vec3d.ofCenter(blockPos), false);
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
        this.pos = Vec3d.ofCenter(this.blockPos).offset(this.direction, d);
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
