package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static TrackedData<Integer> getFROZEN_TICKS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Boolean> getNO_GRAVITY() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<EntityPose> getPOSE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Byte> getFLAGS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getON_FIRE_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getSNEAKING_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getSPRINTING_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getSWIMMING_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getINVISIBLE_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getGLOWING_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static int getFALL_FLYING_FLAG_INDEX() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getAIR() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Optional<Text>> getCUSTOM_NAME() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Boolean> getNAME_VISIBLE() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Boolean> getSILENT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static AtomicInteger getCURRENT_ID() {
        throw new UnsupportedOperationException();
    }
}
