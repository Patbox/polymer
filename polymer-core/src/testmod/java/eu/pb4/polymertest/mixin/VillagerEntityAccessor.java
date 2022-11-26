package eu.pb4.polymertest.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccessor {
    @Accessor("VILLAGER_DATA")
    static TrackedData<VillagerData> get() {
        throw new AssertionError();
    }
}
