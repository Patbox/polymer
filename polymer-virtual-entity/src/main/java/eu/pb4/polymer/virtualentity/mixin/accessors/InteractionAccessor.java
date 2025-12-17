package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Interaction.class)
public interface InteractionAccessor {
    @Accessor
    static EntityDataAccessor<Float> getDATA_WIDTH_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Float> getDATA_HEIGHT_ID() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EntityDataAccessor<Boolean> getDATA_RESPONSE_ID() {
        throw new UnsupportedOperationException();
    }
}
