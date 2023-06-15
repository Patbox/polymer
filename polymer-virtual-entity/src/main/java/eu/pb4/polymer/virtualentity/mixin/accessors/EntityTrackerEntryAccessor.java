package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.entity.TrackedPosition;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityTrackerEntry.class)
public interface EntityTrackerEntryAccessor {
    @Accessor
    TrackedPosition getTrackedPos();
}
