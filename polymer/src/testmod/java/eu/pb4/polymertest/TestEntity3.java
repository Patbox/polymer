package eu.pb4.polymertest;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymertest.mixin.EntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

import java.util.List;

public class TestEntity3 extends CreeperEntity implements PolymerEntity {
    public TestEntity3(EntityType<TestEntity3> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    @Override
    public EntityType<?> getPolymerEntityType() {
        return EntityType.FIREBALL;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
        data.add(new DataTracker.Entry<>(EntityAccessor.getNO_GRAVITY(), true));
    }

}
