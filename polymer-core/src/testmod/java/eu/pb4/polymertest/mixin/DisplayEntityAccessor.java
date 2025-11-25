package eu.pb4.polymertest.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.class)
public interface DisplayEntityAccessor {
    @Accessor
    static TrackedData<Vector3fc> getTRANSLATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Vector3fc> getSCALE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Quaternionfc> getLEFT_ROTATION() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Quaternionfc> getRIGHT_ROTATION() {
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
