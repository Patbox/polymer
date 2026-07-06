package eu.pb4.polymer.virtualentity.impl.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FallingBlockEntityAttachment extends EntityAttachment implements BlockAwareAttachment {
    public FallingBlockEntityAttachment(ElementHolder holder, FallingBlockEntity entity) {
        super(holder, entity, true);
        this.attach();
    }

    @Override
    public BlockPos getBlockPos() {
        return ((FallingBlockEntity) this.entity).getStartPos();
    }

    @Override
    public BlockState getBlockState() {
        return ((FallingBlockEntity) this.entity).getBlockState();
    }

    @Override
    public boolean isPartOfTheWorld() {
        return false;
    }

    @Override
    public Vec3 getPos() {
        return super.getPos().add(0, 0.5, 0);
    }
}
