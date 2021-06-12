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

import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interface used for creation of server-side entities
 */
public interface VirtualEntity extends VirtualObject {
    /**
     * This method is used to determine what this entity will look like on client
     *
     * @return Vanilla/Modded entity type
     */
    default EntityType<?> getVirtualEntityType() {
        return EntityType.ZOMBIE;
    }

    /**
     * This method is used for replacing entity's equipment on client
     *
     * @param map Map of EquipmentSlot and ItemStack on entity server-side
     * @return List of Pair of EquipmentSlot and ItemStack sent to client
     */
    default List<Pair<EquipmentSlot, ItemStack>> getVirtualEntityEquipment(Map<EquipmentSlot, ItemStack> map) {
        List<Pair<EquipmentSlot, ItemStack>> list = Lists.newArrayListWithCapacity(map.size());
        for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
            list.add(Pair.of(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    /**
     * This method can be used to send packets after entity is visible to players
     *
     * @param sender Consumer of packets
     */
    default void sendPackets(Consumer<Packet<?>> sender) {}


    /**
     * This method allows to modify DataTracker values before they are send to the client
     * @param data Current values
     */
    default void modifyTrackedData(List<DataTracker.Entry<?>> data) {}


    /**
     * This method allows to modify position of entity on client
     * @param vec3d Real position
     * @return Client-side position
     */
    default Vec3d getClientSidePosition(Vec3d vec3d) {
        return vec3d;
    }

    /**
     * This method allows to modify yaw of entity on client
     * @param yaw Real yaw value
     * @return Client-side yaw value
     */
    default float getClientSideYaw(float yaw) {
        return yaw;
    }

    /**
     * This method allows to modify head yaw of entity on client
     * @param yaw Real yaw value
     * @return Client-side yaw value
     */
    default float getClientSideHeadYaw(float yaw) {
        return yaw;
    }

    /**
     * This method allows to modify pitch of entity on client
     * @param pitch Real pitch value
     * @return Client-side pitch value
     */
    default float getClientSidePitch(float pitch) {
        return pitch;
    }
}
