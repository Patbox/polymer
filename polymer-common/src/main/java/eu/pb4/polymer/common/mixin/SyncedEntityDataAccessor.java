package eu.pb4.polymer.common.mixin;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.class)
public interface SyncedEntityDataAccessor {
    @Accessor
    SynchedEntityData.DataItem<?>[] getItemsById();
}
