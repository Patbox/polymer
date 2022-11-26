package eu.pb4.polymertest.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static TrackedData<Integer> getFROZEN_TICKS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Boolean> getNO_GRAVITY() {
        throw new UnsupportedOperationException();
    }
}
