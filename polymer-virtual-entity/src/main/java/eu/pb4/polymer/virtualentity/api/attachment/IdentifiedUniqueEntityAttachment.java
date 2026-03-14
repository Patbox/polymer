package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class IdentifiedUniqueEntityAttachment extends EntityAttachment implements UniqueIdentifiableAttachment {
    private final Identifier id;

    public IdentifiedUniqueEntityAttachment(Identifier identifier, ElementHolder holder, Entity entity, boolean autoTick) {
        super(holder, entity, autoTick);
        this.id = identifier;
        if (this.getClass() == IdentifiedUniqueEntityAttachment.class) {
            this.attach();
        }
    }

    public static IdentifiedUniqueEntityAttachment of(Identifier identifier, ElementHolder holder, Entity entity) {
        return new IdentifiedUniqueEntityAttachment(identifier, holder, entity, false);
    }

    public static IdentifiedUniqueEntityAttachment ofTicking(Identifier identifier,ElementHolder holder, Entity entity) {
        return new IdentifiedUniqueEntityAttachment(identifier, holder, entity, true);
    }

    @Nullable
    static UniqueIdentifiableAttachment get(Entity entity, Identifier identifier) {
        return ((HolderAttachmentHolder) entity).polymerVE$getIdHolder(identifier);
    }

    @Override
    public Identifier getAttachmentId() {
        return this.id;
    }
}
