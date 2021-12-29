package eu.pb4.polymer.entity;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Use {@link eu.pb4.polymer.api.entity.PolymerEntity} instead
 */
@Deprecated
public interface VirtualEntity extends PolymerEntity, VirtualObject {
    default EntityType<?> getVirtualEntityType() {
        return EntityType.ZOMBIE;
    }

    @Override
    default EntityType<?> getPolymerEntityType() {
        return getVirtualEntityType();
    }

    default List<Pair<EquipmentSlot, ItemStack>> getVirtualEntityEquipment(Map<EquipmentSlot, ItemStack> map) {
        List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>(map.size());
        for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
            list.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    @Override
    default List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(Map<EquipmentSlot, ItemStack> map) {
        return getVirtualEntityEquipment(map);
    }

    @Deprecated(forRemoval = true)
    default void sendPackets(Consumer<Packet<?>> sender) {}

    default void beforeEntitySpawnPacket(Consumer<Packet<?>> packetConsumer) {}


    default void modifyTrackedData(List<DataTracker.Entry<?>> data) {}


    default Vec3d getClientSidePosition(Vec3d vec3d) {
        return vec3d;
    }

    default float getClientSideYaw(float yaw) {
        return yaw;
    }

    default float getClientSideHeadYaw(float yaw) {
        return yaw;
    }

    default float getClientSidePitch(float pitch) {
        return pitch;
    }
}
