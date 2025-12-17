package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Accessor
    static byte getFLAG_SHADOW() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getFLAG_SEE_THROUGH() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getFLAG_USE_DEFAULT_BACKGROUND() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getFLAG_ALIGN_LEFT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static byte getFLAG_ALIGN_RIGHT() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Component> getDATA_TEXT_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_LINE_WIDTH_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Integer> getDATA_BACKGROUND_COLOR_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Byte> getDATA_TEXT_OPACITY_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Byte> getDATA_STYLE_FLAGS_ID() {
        throw new UnsupportedOperationException();
    }
}
