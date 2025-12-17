package eu.pb4.polymertest.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static EntityDataAccessor<Integer> getDATA_TICKS_FROZEN() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getDATA_NO_GRAVITY() {
        throw new UnsupportedOperationException();
    }
}
