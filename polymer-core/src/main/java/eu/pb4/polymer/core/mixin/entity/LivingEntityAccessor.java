package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor
    static EntityDataAccessor<Byte> getDATA_LIVING_ENTITY_FLAGS() {
        throw new UnsupportedOperationException();
    }
}
