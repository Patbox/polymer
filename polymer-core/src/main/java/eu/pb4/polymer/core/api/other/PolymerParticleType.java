package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PolymerParticleType<T extends ParticleOptions> extends PolymerSyncedObject<ParticleType<?>> {
    @Override
    default ParticleType<?> getPolymerReplacement(ParticleType<?> object, PacketContext context) {
        return ParticleTypes.ANGRY_VILLAGER;
    }

    ParticleOptions getPolymerParticleReplacement(T options, PacketContext context);

    static <T extends ParticleOptions> void setOverlay(ParticleType<T> type, PolymerParticleType<T> overlay) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.PARTICLE_TYPE, type, overlay);
    }

    @Nullable
    static <T extends ParticleOptions> PolymerParticleType<T> getOverlay(ParticleType<T> type) {
        //noinspection unchecked
        return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.PARTICLE_TYPE, type) instanceof PolymerParticleType<?> polymerParticleType ? (PolymerParticleType<T>) polymerParticleType : null;
    }
}
