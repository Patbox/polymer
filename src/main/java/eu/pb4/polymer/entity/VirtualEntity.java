package eu.pb4.polymer.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public interface VirtualEntity extends VirtualObject {
    default EntityType<?> getVirtualEntityType() {
        return EntityType.ZOMBIE;
    }

    default List<Pair<EquipmentSlot, ItemStack>> getVirtualEntityEquipment(Map<EquipmentSlot, ItemStack> map) {
        List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(map.size());
        for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
            list.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    default void sendPacketsAfterCreation(Consumer<Packet<?>> sender) {}

    default void modifyTrackedData(List<DataTracker.Entry<?>> data) {}
}
