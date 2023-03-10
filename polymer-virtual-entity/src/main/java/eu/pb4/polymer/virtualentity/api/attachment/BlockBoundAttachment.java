package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class BlockBoundAttachment extends ChunkAttachment {
    private final BlockPos blockPos;
    private BlockState blockState;

    @ApiStatus.Internal
    public BlockBoundAttachment(ElementHolder holder, WorldChunk chunk, BlockState state, BlockPos blockPos, Vec3d position, boolean autoTick) {
        super(holder, chunk, position, autoTick);
        this.blockPos = blockPos;
        this.blockState = state;
        this.attach();
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
    }

    public BlockState getBlockState() {
        return this.blockState;
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
}
