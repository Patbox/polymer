package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public interface BlockAwareAttachment extends HolderAttachment {
    UpdateType BLOCK_STATE_UPDATE = UpdateType.of("BlockState");

    BlockPos getBlockPos();
    BlockState getBlockState();
    boolean isPartOfTheWorld();

    @Nullable
    static BlockAwareAttachment get(Level world, BlockPos pos) {
        var chunk = world.getChunk(pos);
        return chunk instanceof LevelChunk worldChunk ? get(worldChunk, pos) : null;
    }

    @Nullable
    static BlockAwareAttachment get(LevelChunk chunk, BlockPos pos) {
        return ((HolderAttachmentHolder) chunk).polymerVE$getPosHolder(pos);
    }

    @Nullable
    static BlockAwareAttachment get(ElementHolder holder) {
        return holder.getAttachment() instanceof BlockAwareAttachment blockBoundAttachment ? blockBoundAttachment : null;
    }
}
