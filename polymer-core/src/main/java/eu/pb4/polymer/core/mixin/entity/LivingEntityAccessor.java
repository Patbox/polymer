package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor
    static TrackedData<Byte> getLIVING_FLAGS() {
        throw new UnsupportedOperationException();
    }
}
