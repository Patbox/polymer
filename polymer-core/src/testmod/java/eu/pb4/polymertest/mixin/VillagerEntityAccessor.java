package eu.pb4.polymertest.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Villager.class)
public interface VillagerEntityAccessor {
    @Accessor("DATA_VILLAGER_DATA")
    static EntityDataAccessor<VillagerData> get() {
        throw new AssertionError();
    }
}
