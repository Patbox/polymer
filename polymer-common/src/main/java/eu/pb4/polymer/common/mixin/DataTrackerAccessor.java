package eu.pb4.polymer.common.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataTracker.class)
public interface DataTrackerAccessor {
    @Accessor
    DataTracker.Entry<?>[] getEntries();
}
