package eu.pb4.polymertest.mixin;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AreaEffectCloudEntity.class)
public interface AECAccessor {
    @Accessor("RADIUS")
    static TrackedData<Float> getRadius() {
        throw new AssertionError();
    }

    @Accessor("COLOR")
    static TrackedData<Integer> getColor() {
        throw new AssertionError();
    }

    @Accessor("WAITING")
    static TrackedData<Boolean> getWaiting() {
        throw new AssertionError();
    }

    @Accessor("PARTICLE_ID")
    static TrackedData<ParticleEffect> getParticle() {
        throw new AssertionError();
    }
}
