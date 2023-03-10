package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public interface HolderAttachmentHolder {
    void polymerVE$addHolder(HolderAttachment holderAttachment);
    void polymerVE$removeHolder(HolderAttachment holderAttachment);

    default void polymerVE$removePosHolder(BlockPos pos) {}
    default BlockBoundAttachment polymerVE$getPosHolder(BlockPos pos) {
        return null;
    }
    Collection<HolderAttachment> polymerVE$getHolders();
}
