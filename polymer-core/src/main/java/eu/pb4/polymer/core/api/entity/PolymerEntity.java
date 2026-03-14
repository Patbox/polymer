package eu.pb4.polymer.core.api.entity;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.interfaces.PolymerEntityProvider;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Interface used for creation of server-side entities
 */
public interface PolymerEntity extends PolymerObject {
    /**
     * This method is used to determine what this entity will look like on client for specific player
     *
     * @return Vanilla/Modded entity type
     */
    EntityType<?> getPolymerEntityType(PacketContext context);

    /**
     * This method is used for replacing entity's equipment on client for a player
     *
     * @param items List of a Pair of EquipmentSlot and ItemStack on entity server-side
     * @return List of a Pair of EquipmentSlot and ItemStack sent to client
     */
    default List<Pair<EquipmentSlot, ItemStack>> getPolymerVisibleEquipment(List<Pair<EquipmentSlot, ItemStack>> items, ServerPlayer player) {
        return items;
    }

    /**
     * Allows sending packets before entity's spawn packet, useful for Player Entities
     */
    default void onBeforeSpawnPacket(ServerPlayer player, Consumer<Packet<?>> packetConsumer) {}

    /**
     * This method allows to modify raw serialized DataTracker entries before they are send to the client
     * @param data Current values
     * @param initial
     */
    default void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {

    }

    default void modifyRawEntityAttributeData(List<ClientboundUpdateAttributesPacket.AttributeSnapshot> data, ServerPlayer player, boolean initial) {

    }


    default void onEntityPacketSent(Consumer<Packet<?>> consumer, Packet<?> packet) {
        consumer.accept(packet);
    }

    /**
     * Allows disabling sending packets to player
     * @param player
     * @return true to allow, false to disable
     */
    default boolean sendPacketsTo(ServerPlayer player) {
        return true;
    }

    /**
     * This method is executed after tracker tick
     */
    default void onEntityTrackerTick(Set<ServerPlayerConnection> listeners) {};

    default void beforeEntityTrackerTick(Set<ServerPlayerConnection> listeners) {}

    /**
     * Sends real id to clients with polymer
     */
    default boolean canSynchronizeToPolymerClient(ServerPlayer player) {
        return true;
    }

    default boolean sendEmptyTrackerUpdates(ServerPlayer player) {
        return true;
    }

    default boolean isPolymerEntityInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, InteractionResult actionResult) {
        return true;
    }
    @Nullable
    static PolymerEntity get(@Nullable Entity entity) {
        return entity != null ? ((PolymerEntityProvider) entity).polymer$getPolymerEntity() : null;
    }
}
