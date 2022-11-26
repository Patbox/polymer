package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static AtomicInteger getCURRENT_ID() {
        throw new UnsupportedOperationException();
    }
}
