package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface UniqueIdentifiableAttachment extends HolderAttachment {
    Identifier getAttachmentId();

    @Nullable
    static UniqueIdentifiableAttachment get(Entity entity, Identifier identifier) {
        return ((HolderAttachmentHolder) entity).polymerVE$getIdHolder(identifier);
    }
}
