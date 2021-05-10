package eu.pb4.polymertest;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.entity.VirtualEntity;
import eu.pb4.polymertest.mixin.VillagerEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class TestEntity extends CreeperEntity implements VirtualEntity {
    public TestEntity(EntityType<TestEntity> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    public TestEntity(World world) {
        super(TestMod.entity, world);
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
        return EntityType.VILLAGER;
    }

    @Override
    public void modifyTrackedData(List<DataTracker.Entry<?>> data) {
        data.add(new DataTracker.Entry<>(VillagerEntityAccessor.get(), new VillagerData(VillagerType.SWAMP, VillagerProfession.CARTOGRAPHER, 1)));
    }
}
