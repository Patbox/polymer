package eu.pb4.polymer.virtualentity.api.elements;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.mixin.LivingEntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityTrackerEntryAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EntityElement<T extends Entity> extends AbstractElement {
    private final T entity;
    private final EntityTrackerEntry entry;

    public EntityElement(T entity, ServerWorld world) {
        this(entity, world, InteractionHandler.EMPTY);
    }

    public EntityElement(T entity, ServerWorld world, InteractionHandler handler) {
        this.entity = entity;
        this.entry = new EntityTrackerEntry(world, this.entity, 1, false, this::sendPacket);
        this.setInteractionHandler(handler);
    }

    private void sendPacket(Packet<?> packet) {
        if (this.getHolder() != null) {
            this.getHolder().sendPacket((Packet<ClientPlayPacketListener>) packet);
        }
    }

    public EntityElement(EntityType<T> entityType, ServerWorld world) {
        this(entityType.create(world), world);
    }

    public EntityElement(EntityType<T> entityType, ServerWorld world, InteractionHandler handler) {
        this(entityType.create(world), world);
    }

    public T entity() {
        return this.entity;
    }

    @Override
    public IntList getEntityIds() {
        return IntList.of(this.entity.getId());
    }

    @Override
    public void setHolder(@Nullable ElementHolder holder) {
        super.setHolder(holder);
        if (holder != null) {
            var pos = holder.getPos().add(this.getOffset());
            this.entity.setPos(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void setOffset(Vec3d vec3d) {
        super.setOffset(vec3d);
        if (this.getHolder() != null) {
            var pos = this.getHolder().getPos().add(vec3d);
            this.entity.setPos(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        this.entry.sendPackets(player, packetConsumer);
    }

    @Override
    public void stopWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
       packetConsumer.accept(new EntitiesDestroyS2CPacket(this.entity.getId()));
    }

    @Override
    public void notifyMove(Vec3d oldPos, Vec3d currentPos, Vec3d delta) {
        if (this.getHolder() != null) {
            var pos = currentPos.add(this.getOffset());
            this.entity.setPos(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void setInitialPosition(Vec3d pos) {
        pos = pos.add(this.getOffset());
        this.entity.setPosition(pos);
        ((EntityTrackerEntryAccessor) this.entry).getTrackedPos().setPos(pos);
    }

    @Override
    public void tick() {
        this.entry.tick();
        if (this.entity instanceof LivingEntity livingEntity) {
            this.sendEquipmentChanges(livingEntity);
        }
    }

    private void sendEquipmentChanges(LivingEntity livingEntity) {
        var ac = ((LivingEntityAccessor) livingEntity);
        var equipmentChanges = ac.callGetEquipmentChanges();
        if (equipmentChanges != null && !equipmentChanges.isEmpty()) {
            List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>(equipmentChanges.size());
            equipmentChanges.forEach((slot, stack) -> {
                ItemStack itemStack = stack.copy();
                list.add(Pair.of(slot, itemStack));
                switch (slot.getType()) {
                    case HAND:
                        ac.callSetSyncedHandStack(slot, itemStack);
                        break;
                    case ARMOR:
                       ac.callSetSyncedArmorStack(slot, itemStack);
                }
            });

            this.sendPacket(new EntityEquipmentUpdateS2CPacket(livingEntity.getId(), list));
        }
    }
}
