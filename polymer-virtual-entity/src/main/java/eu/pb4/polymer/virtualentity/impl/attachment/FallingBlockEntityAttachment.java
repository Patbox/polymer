package eu.pb4.polymer.virtualentity.impl.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FallingBlockEntityAttachment extends EntityAttachment implements BlockAwareAttachment {
    public FallingBlockEntityAttachment(ElementHolder holder, FallingBlockEntity entity) {
        super(holder, entity, true);
    }

    @Override
    public BlockPos getBlockPos() {
        return ((FallingBlockEntity) this.entity).getFallingBlockPos();
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
    public Vec3d getPos() {
        return super.getPos().add(0, 0.5, 0);
    }
}
