package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.gen.Invoker;

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

    @Accessor
    static EntityDataAccessor<Pose> getDATA_POSE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Byte> getDATA_SHARED_FLAGS_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_ONFIRE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_SHIFT_KEY_DOWN() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_SPRINTING() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_SWIMMING() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_INVISIBLE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_GLOWING() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFLAG_FALL_FLYING() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_AIR_SUPPLY_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Optional<Component>> getDATA_CUSTOM_NAME() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getDATA_CUSTOM_NAME_VISIBLE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getDATA_SILENT() {
        throw new UnsupportedOperationException();
    }

    @Invoker
    AABB callMakeBoundingBox(final Vec3 position);
}
