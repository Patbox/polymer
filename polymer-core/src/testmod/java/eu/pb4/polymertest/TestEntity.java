package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymertest.mixin.VillagerEntityAccessor;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class TestEntity extends Creeper implements PolymerEntity {
    public TestEntity(EntityType<TestEntity> entityEntityType, Level world) {
        super(entityEntityType, world);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext context) {
        return EntityType.VILLAGER;
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        if (initial) {
            //data.add(new DataTracker.SerializedEntry(VillagerEntityAccessor.get().id(), VillagerEntityAccessor.get().dataType(), new VillagerData(VillagerType.SWAMP, VillagerProfession.CARTOGRAPHER, 1)));
        }
    }

    //@Override
    //protected SoundEvent getHurtSound(DamageSource source) {
    //    return TestMod.GHOST_HURT;
    //}
}
