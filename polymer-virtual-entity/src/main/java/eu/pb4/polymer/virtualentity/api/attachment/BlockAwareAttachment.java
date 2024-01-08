package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public interface BlockAwareAttachment extends HolderAttachment {
    UpdateType BLOCK_STATE_UPDATE = UpdateType.of("BlockState");

    BlockPos getBlockPos();
    BlockState getBlockState();
    boolean isPartOfTheWorld();

    @Nullable
    static BlockAwareAttachment get(World world, BlockPos pos) {
        var chunk = world.getChunk(pos);
        return chunk instanceof WorldChunk worldChunk ? get(worldChunk, pos) : null;
    }

    @Nullable
    static BlockAwareAttachment get(WorldChunk chunk, BlockPos pos) {
        return ((HolderAttachmentHolder) chunk).polymerVE$getPosHolder(pos);
    }

    @Nullable
    static BlockAwareAttachment get(ElementHolder holder) {
        return holder.getAttachment() instanceof BlockAwareAttachment blockBoundAttachment ? blockBoundAttachment : null;
    }
}
