package eu.pb4.polymertest.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.class)
public interface DisplayEntityAccessor {
    @Accessor
    static TrackedData<Vector3f> getTRANSLATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Vector3f> getSCALE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Quaternionf> getLEFT_ROTATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Quaternionf> getRIGHT_ROTATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getINTERPOLATION_DURATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getBRIGHTNESS() {
        throw new UnsupportedOperationException();
    }
}
