package eu.pb4.polymertest;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.entity.VirtualEntity;
import eu.pb4.polymertest.mixin.AECAccessor;
import eu.pb4.polymertest.mixin.VillagerEntityAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class TestEntity2 extends CreeperEntity implements VirtualEntity {
    public TestEntity2(EntityType<TestEntity2> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    public TestEntity2(World world) {
        super(TestMod.entity2, world);
    }

    @Override
    public List<Pair<EquipmentSlot, ItemStack>> getVirtualEntityEquipment(Map<EquipmentSlot, ItemStack> map) {
        List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(map.size());
        list.add(Pair.of(EquipmentSlot.MAINHAND, Items.DIAMOND.getDefaultStack()));
        list.add(Pair.of(EquipmentSlot.HEAD, TestMod.blockItemTater.getDefaultStack()));
        return list;
    }

    @Override
    public EntityType<?> getVirtualEntityType() {
        return EntityType.AREA_EFFECT_CLOUD;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
        data.add(new DataTracker.Entry<>(AECAccessor.getColor(), 0x03f4fc));
        data.add(new DataTracker.Entry<>(AECAccessor.getRadius(), 0.5f));
        data.add(new DataTracker.Entry<>(AECAccessor.getParticle(), ParticleTypes.FIREWORK));
    }
}
