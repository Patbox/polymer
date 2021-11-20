package eu.pb4.polymertest;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymertest.mixin.VillagerEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

import java.util.List;

public class TestEntity extends CreeperEntity implements PolymerEntity {
    public TestEntity(EntityType<TestEntity> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    public TestEntity(World world) {
        super(TestMod.ENTITY, world);
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.VILLAGER;
    }

    @Override
    public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
        data.add(new DataTracker.Entry<>(VillagerEntityAccessor.get(), new VillagerData(VillagerType.SWAMP, VillagerProfession.CARTOGRAPHER, 1)));
    }

    @Override
    public float getClientSidePitch(float pitch) {
        return 3.14f;
    }

    @Override
    public Vec3d getClientSidePosition(Vec3d vec3d) {
        return vec3d.add(1, 1, 1);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TestMod.GHOST_HURT;
    }
}
