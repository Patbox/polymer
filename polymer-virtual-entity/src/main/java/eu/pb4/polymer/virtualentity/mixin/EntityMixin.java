package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(Entity.class)
public abstract class EntityMixin implements HolderAttachmentHolder, EntityExt {
    @Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Unique
    private final Collection<HolderAttachment> polymerVE$holders = new ArrayList<>();
    @Unique
    private final IntList polymerVE$virtualRidden = new IntArrayList();
    @Unique
    private boolean polymerVE$virtualRiddenDirty = false;

    @Override
    public void polymerVE$addHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.add(holderAttachment);
    }

    @Override
    public void polymerVE$removeHolder(HolderAttachment holderAttachment) {
        this.polymerVE$holders.remove(holderAttachment);
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
