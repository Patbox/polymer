package eu.pb4.polymer.mixin.entity;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecartEntity.class)
public interface AbstractMinecartEntityAccessor {
    @Accessor
    static TrackedData<Integer> getCUSTOM_BLOCK_ID() {
        throw new UnsupportedOperationException();
    }
}
