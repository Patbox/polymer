package eu.pb4.polymer.mixin.entity;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor
    static TrackedData<VillagerData> getVILLAGER_DATA() {
        throw new UnsupportedOperationException();
    }
}
