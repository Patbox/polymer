package eu.pb4.polymertest.mixin;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AreaEffectCloud.class)
public interface AECAccessor {
    @Accessor("DATA_RADIUS")
    static EntityDataAccessor<Float> getRadius() {
        throw new AssertionError();
    }

    @Accessor("DATA_WAITING")
    static EntityDataAccessor<Boolean> getWaiting() {
        throw new AssertionError();
    }

    @Accessor("DATA_PARTICLE")
    static EntityDataAccessor<ParticleOptions> getParticle() {
        throw new AssertionError();
    }
}
