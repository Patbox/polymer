package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.class)
public interface DisplayAccessor {
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

    @Accessor
    static EntityDataAccessor<Float> getDATA_VIEW_RANGE_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Float> getDATA_SHADOW_RADIUS_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Float> getDATA_SHADOW_STRENGTH_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Float> getDATA_WIDTH_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Float> getDATA_HEIGHT_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_GLOW_COLOR_OVERRIDE_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Byte> getDATA_BILLBOARD_RENDER_CONSTRAINTS_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_POS_ROT_INTERPOLATION_DURATION_ID() {
        throw new UnsupportedOperationException();
    }
}
