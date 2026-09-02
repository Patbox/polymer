package eu.pb4.polymer.virtualentity.api.elements;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.mixin.LivingEntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ServerEntityAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityElement<T extends Entity> extends AbstractElement {
    private final T entity;
    private final ServerEntity entry;
    private final Map<EquipmentSlot, ItemStack> equipmentSlotItemStackEnumMap = new EnumMap<>(EquipmentSlot.class);

    public EntityElement(T entity, ServerLevel world) {
        this(entity, world, InteractionHandler.EMPTY);
    }

    public EntityElement(T entity, ServerLevel world, InteractionHandler handler) {
        this.entity = entity;
        this.entry = new ServerEntity(world, this.entity, new UpdateInterval() {
            @Override
            public boolean test(int tick) {
                return true;
            }

            @Override
            public int nextInterval(int tick) {
                return tick + 1;
            }
        }, false, new ServerEntity.Synchronizer() {
            @Override
            public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
                sendPacket((Packet<ClientGamePacketListener>) packet);
            }

            @Override
            public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
                sendToTrackingPlayers(packet);
            }

            @Override
            public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {
                sendPacket((Packet<ClientGamePacketListener>) packet, predicate);
            }
        });
        this.setInteractionHandler(handler);
    }

    public EntityElement(EntityType<T> entityType, ServerLevel world) {
        this(entityType.create(world, EntitySpawnReason.LOAD), world);
    }

    public EntityElement(EntityType<T> entityType, ServerLevel world, InteractionHandler handler) {
        this(entityType.create(world, EntitySpawnReason.LOAD), world);
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
            var pos = this.getCurrentPos();
            this.entity.setPosRaw(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void setOffset(Vec3 vec3d) {
        super.setOffset(vec3d);
        if (this.getOverridePos() == null && this.getHolder() != null) {
            var pos = this.getHolder().getPos().add(vec3d);
            this.entity.setPosRaw(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void setOverridePos(Vec3 vec3d) {
        super.setOverridePos(vec3d);
        if (this.getHolder() != null) {
            this.entity.setPosRaw(vec3d.x, vec3d.y, vec3d.z);
        }
    }

    @Override
    public void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        if (!this.elementVisiblityPredicate.test(player)) {
            return;
        }
        this.entry.sendPairingData(player, packetConsumer);
    }

    @Override
    public void stopWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        if (!this.elementVisiblityPredicate.test(player)) {
            return;
        }
       packetConsumer.accept(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
    }

    @Override
    public void notifyMove(Vec3 oldPos, Vec3 currentPos, Vec3 delta) {
        if (this.getOverridePos() == null && this.getHolder() != null) {
            var pos = currentPos.add(this.getOffset());
            this.entity.setPosRaw(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void setInitialPosition(Vec3 pos) {
        if (this.getOverridePos() == null) {
            pos = pos.add(this.getOffset());
            this.entity.setPos(pos);
            ((ServerEntityAccessor) this.entry).getPositionCodec().setBase(pos);
        } else {
            ((ServerEntityAccessor) this.entry).getPositionCodec().setBase(this.getOverridePos());
        }
    }

    @Override
    public Vec3 getLastSyncedPos() {
        return this.entry.getPositionBase();
    }

    @Override
    public void tick() {
        this.entry.sendChanges();
        if (this.entity instanceof LivingEntity livingEntity) {
            this.sendEquipmentChanges(livingEntity);
        }
    }

    private void sendEquipmentChanges(LivingEntity livingEntity) {
        var ac = ((LivingEntityAccessor) livingEntity);
        var equipmentChanges = ac.callCollectEquipmentChanges(ac.polymer$getLastEquipmentItems());
        if (equipmentChanges != null && !equipmentChanges.isEmpty()) {
            List<Pair<EquipmentSlot, ItemStack>> list = new ArrayList<>(equipmentChanges.size());
            equipmentChanges.forEach((slot, stack) -> {
                ItemStack itemStack = stack.copy();
                list.add(Pair.of(slot, itemStack));
                //ac.callSetSyncedArmorStack(slot, itemStack);
            });

            if (this.getHolder() != null) {
                sendPacket(new ClientboundSetEquipmentPacket(livingEntity.getId(), list));
            }
        }
    }
}
