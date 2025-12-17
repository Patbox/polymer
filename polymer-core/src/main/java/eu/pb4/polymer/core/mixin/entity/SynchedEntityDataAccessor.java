package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.class)
public interface SynchedEntityDataAccessor {
    @Accessor
    SynchedEntityData.DataItem<?>[] getItemsById();
}
