package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.UniqueIdentifiableAttachment;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public abstract class EntityMixin implements HolderAttachmentHolder, EntityExt {
    @Unique
    private final Collection<HolderAttachment> polymerVE$holders = new ArrayList<>();
    @Unique
    private final Map<Identifier, UniqueIdentifiableAttachment> polymerVE$identifiedHolders = new HashMap<>();
    @Unique
    private final IntList polymerVE$virtualRidden = new IntArrayList();
    @Unique
    private boolean polymerVE$virtualRiddenDirty = false;

    @Override
    public void polymerVE$addHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.add(holderAttachment);
        if (holderAttachment instanceof UniqueIdentifiableAttachment idAttachment) {
            this.polymerVE$identifiedHolders.put(idAttachment.getAttachmentId(), idAttachment);
        }
    }

    @Override
    public void polymerVE$removeHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.remove(holderAttachment);
        if (holderAttachment instanceof UniqueIdentifiableAttachment idAttachment) {
            this.polymerVE$identifiedHolders.remove(idAttachment.getAttachmentId(), idAttachment);
        }
    }

    @Override
    public UniqueIdentifiableAttachment polymerVE$getIdHolder(Identifier identifier) {
        return this.polymerVE$identifiedHolders.get(identifier);
    }

    @Override
    public Collection<HolderAttachment> polymerVE$getHolders() {
        return this.polymerVE$holders;
    }

    @Override
    public IntList polymerVE$getVirtualRidden() {
        return this.polymerVE$virtualRidden;
    }

    @Override
    public void polymerVE$markVirtualRiddenDirty() {
        this.polymerVE$virtualRiddenDirty = true;
    }

    @Override
    public boolean polymerVE$getAndClearVirtualRiddenDirty() {
        var old = this.polymerVE$virtualRiddenDirty;
        this.polymerVE$virtualRiddenDirty = false;
        return old;
    }
}
