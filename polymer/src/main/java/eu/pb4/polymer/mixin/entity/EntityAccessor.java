package eu.pb4.polymer.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static TrackedData<Integer> getFROZEN_TICKS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static AtomicInteger getCURRENT_ID() {
        throw new UnsupportedOperationException();
    }
}
