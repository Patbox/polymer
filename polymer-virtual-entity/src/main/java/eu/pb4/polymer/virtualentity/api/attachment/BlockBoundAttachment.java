package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class BlockBoundAttachment extends ChunkAttachment implements BlockAwareAttachment {
    public static final UpdateType BLOCK_STATE_UPDATE = BlockAwareAttachment.BLOCK_STATE_UPDATE;

    private final BlockPos blockPos;
    private BlockState blockState;

    @ApiStatus.Internal
    public BlockBoundAttachment(ElementHolder holder, LevelChunk chunk, BlockState state, BlockPos blockPos, Vec3 position, boolean autoTick) {
        super(holder, chunk, position, autoTick);
        this.blockPos = blockPos;
        this.blockState = state;
        this.attach();
    }

    @ApiStatus.Experimental
    @Nullable
    public static BlockBoundAttachment of(ElementHolder holder, ServerLevel serverWorld, BlockPos blockPos, BlockState state) {
        return of(holder, serverWorld, serverWorld.getChunkAt(blockPos), blockPos, state);
    }
    @ApiStatus.Experimental
    @Nullable
    public static BlockBoundAttachment of(ElementHolder holder, ServerLevel serverWorld, LevelChunk worldChunk, BlockPos blockPos, BlockState state) {
        var blockWithElementHolder = BlockWithElementHolder.get(state);
        if (blockWithElementHolder != null) {
            return new BlockBoundAttachment(holder, worldChunk, state, blockPos,
                    Vec3.atCenterOf(blockPos).add(blockWithElementHolder.getElementHolderOffset(serverWorld, blockPos, state)),
                    blockWithElementHolder.tickElementHolder(serverWorld, blockPos, state)
            );
        }
        return null;
    }

    @ApiStatus.Experimental
    @Nullable
    public static BlockBoundAttachment fromMoving(ElementHolder movingHolder, ServerLevel world, BlockPos pos, BlockState state) {
        var withElementHolder = BlockWithElementHolder.get(state);
        if (withElementHolder != null) {
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
    public static BlockBoundAttachment get(Level world, BlockPos pos) {
        var chunk = world.getChunk(pos);
        return chunk instanceof LevelChunk worldChunk ? get(worldChunk, pos) : null;
    }

    @Nullable
    public static BlockBoundAttachment get(LevelChunk chunk, BlockPos pos) {
        return ((HolderAttachmentHolder) chunk).polymerVE$getPosHolder(pos);
    }

    @Nullable
    public static BlockBoundAttachment get(ElementHolder holder) {
        return holder.getAttachment() instanceof BlockBoundAttachment blockBoundAttachment ? blockBoundAttachment : null;
    }
}
