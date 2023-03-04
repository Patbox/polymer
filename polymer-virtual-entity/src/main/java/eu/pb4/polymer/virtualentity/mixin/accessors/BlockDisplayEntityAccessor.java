package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.block.BlockState;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.BlockDisplayEntity.class)
public interface BlockDisplayEntityAccessor {
    @Accessor
    static TrackedData<BlockState> getBLOCK_STATE() {
        throw new UnsupportedOperationException();
    }
}
