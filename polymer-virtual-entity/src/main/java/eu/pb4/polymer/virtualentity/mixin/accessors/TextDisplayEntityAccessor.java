package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public interface TextDisplayEntityAccessor {
    @Accessor
    static byte getSHADOW_FLAG() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getSEE_THROUGH_FLAG() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getDEFAULT_BACKGROUND_FLAG() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getLEFT_ALIGNMENT_FLAG() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getRIGHT_ALIGNMENT_FLAG() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Text> getTEXT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getLINE_WIDTH() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Integer> getBACKGROUND() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Byte> getTEXT_OPACITY() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static TrackedData<Byte> getTEXT_DISPLAY_FLAGS() {
        throw new UnsupportedOperationException();
    }
}
