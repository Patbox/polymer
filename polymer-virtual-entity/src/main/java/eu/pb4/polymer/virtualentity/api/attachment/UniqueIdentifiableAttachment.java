package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface UniqueIdentifiableAttachment extends HolderAttachment {
    Identifier getAttachmentId();

    @Nullable
    static UniqueIdentifiableAttachment get(Entity entity, Identifier identifier) {
        return ((HolderAttachmentHolder) entity).polymerVE$getIdHolder(identifier);
    }
}
