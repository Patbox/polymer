package eu.pb4.polymer.core.api.entity;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface used for creation of server-side entities
 */
public interface PolymerEntity extends PolymerObject {
    /**
     * This method is used to determine what this entity will look like on client for specific player
     *
     * @return Vanilla/Modded entity type
     */
    EntityType<?> getPolymerEntityType(ServerPlayerEntity player);

    /**
     * This method is used for replacing entity's equipment on client for a player
     *
     * @param items List of Pair of EquipmentSlot and ItemStack on entity server-side
     * @return List of Pair of EquipmentSlot and ItemStack sent to client
     */
    default List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<Pair<EquipmentSlot, ItemStack>> items, ServerPlayerEntity player) {
        return items;
    }

    /**
     * Allows sending packets before entity's spawn packet, useful for Player Entities
     */
    default void onBeforeSpawnPacket(Consumer<Packet<?>> packetConsumer) {}

    /**
     * This method allows to modify raw serialized DataTracker entries before they are send to the client
     * @param data Current values
     * @param initial
     */
    default void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial) {

    }

    default void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        consumer.accept(packet);
    }

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
     * @param yaw Real yaw stateValue
     * @return Client-side yaw stateValue
     */
    default float getClientSideYaw(float yaw) {
        return yaw;
    }

    /**
     * This method allows to modify head yaw of entity on client
     * @param yaw Real yaw stateValue
     * @return Client-side yaw stateValue
     */
    default float getClientSideHeadYaw(float yaw) {
        return yaw;
    }

    /**
     * This method allows to modify pitch of entity on client
     * @param pitch Real pitch stateValue
     * @return Client-side pitch stateValue
     */
    default float getClientSidePitch(float pitch) {
        return pitch;
    }

    /**
     * Allows disabling sending packets to player
     * @param player
     * @return true to allow, false to disable
     */
    default boolean sendPacketsTo(ServerPlayerEntity player) {
        return true;
    }

    /**
     * This method is executed after tracker tick
     */
    default void onEntityTrackerTick(Set<EntityTrackingListener> listeners) {};

    /**
     * Sends real id to clients with polymer
     */
    default boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return true;
    }
}
