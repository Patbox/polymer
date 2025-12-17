package eu.pb4.polymertest.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayEntityAccessor {
    @Accessor
    static EntityDataAccessor<Vector3fc> getDATA_TRANSLATION_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Vector3fc> getDATA_SCALE_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Quaternionfc> getDATA_LEFT_ROTATION_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Quaternionfc> getDATA_RIGHT_ROTATION_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_TRANSFORMATION_INTERPOLATION_DURATION_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_BRIGHTNESS_OVERRIDE_ID() {
        throw new UnsupportedOperationException();
    }
}
