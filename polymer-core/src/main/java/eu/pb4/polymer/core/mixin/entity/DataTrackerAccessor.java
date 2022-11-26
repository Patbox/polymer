package eu.pb4.polymer.core.mixin.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataTracker.class)
public interface DataTrackerAccessor {
    @Accessor("trackedEntity")
    Entity getTrackedEntity();

    @Accessor
    Int2ObjectMap<DataTracker.Entry<?>> getEntries();
}
