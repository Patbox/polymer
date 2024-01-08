package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class BlockBoundAttachment extends ChunkAttachment implements BlockAwareAttachment {
    public static final UpdateType BLOCK_STATE_UPDATE = BlockAwareAttachment.BLOCK_STATE_UPDATE;

    private final BlockPos blockPos;
    private BlockState blockState;

    @ApiStatus.Internal
    public BlockBoundAttachment(ElementHolder holder, WorldChunk chunk, BlockState state, BlockPos blockPos, Vec3d position, boolean autoTick) {
        super(holder, chunk, position, autoTick);
        this.blockPos = blockPos;
        this.blockState = state;
        this.attach();
    }

    @ApiStatus.Experimental
    @Nullable
    public static BlockBoundAttachment of(ElementHolder holder, ServerWorld serverWorld, BlockPos blockPos, BlockState state) {
        return of(holder, serverWorld, serverWorld.getWorldChunk(blockPos), blockPos, state);
    }
    @ApiStatus.Experimental
    @Nullable
    public static BlockBoundAttachment of(ElementHolder holder, ServerWorld serverWorld, WorldChunk worldChunk, BlockPos blockPos, BlockState state) {
        if (state.getBlock() instanceof BlockWithElementHolder blockWithElementHolder) {
            return new BlockBoundAttachment(holder, worldChunk, state, blockPos,
                    Vec3d.ofCenter(blockPos).add(blockWithElementHolder.getElementHolderOffset(serverWorld, blockPos, state)),
                    blockWithElementHolder.tickElementHolder(serverWorld, blockPos, state)
            );
        }
        return null;
    }

    @ApiStatus.Experimental
    @Nullable
    public static BlockBoundAttachment fromMoving(ElementHolder movingHolder, ServerWorld world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BlockWithElementHolder withElementHolder) {
            var x = withElementHolder.createStaticElementHolder(world, pos, state, movingHolder);
            if (x != movingHolder) {
                movingHolder.destroy();
            } else if (movingHolder.getAttachment() != null) {
                var y = movingHolder.getAttachment();
                movingHolder.setAttachment(null);
                y.destroy();
            }

            return of(x, world, pos, state);
        } else if (movingHolder.getAttachment() != null) {
            movingHolder.destroy();
        }
        return null;
    }

    @Override
    protected void attach() {
        if (this.blockPos != null) {
            super.attach();
        }
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

    @Nullable
    public static BlockBoundAttachment get(World world, BlockPos pos) {
        var chunk = world.getChunk(pos);
        return chunk instanceof WorldChunk worldChunk ? get(worldChunk, pos) : null;
    }

    @Nullable
    public static BlockBoundAttachment get(WorldChunk chunk, BlockPos pos) {
        return ((HolderAttachmentHolder) chunk).polymerVE$getPosHolder(pos);
    }

    @Nullable
    public static BlockBoundAttachment get(ElementHolder holder) {
        return holder.getAttachment() instanceof BlockBoundAttachment blockBoundAttachment ? blockBoundAttachment : null;
    }
}
