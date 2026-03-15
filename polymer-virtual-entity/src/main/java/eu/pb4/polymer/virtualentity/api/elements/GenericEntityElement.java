package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.data.SynchedEntityDataLike;
import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.api.data.SimpleSynchedEntityData;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("ConstantConditions")
public abstract class GenericEntityElement extends AbstractElement {
    protected final SynchedEntityDataLike dataTracker = this.createDataTracker();
    private final int id = VirtualEntityUtils.requestEntityId();
    private final UUID uuid = UUID.randomUUID();
    private float pitch;
    private float yaw;
    private boolean isRotationDirty;
    private boolean sendPositionUpdates = true;
    private boolean instantPositionUpdates = false;
    private boolean alwaysSyncAbsolutePosition = false;

    private int updatesSinceLastAbsolutePositionSync = 0;

    protected SynchedEntityDataLike createDataTracker() {
        return new SimpleSynchedEntityData(this.getEntityType());
    }

    public boolean isDirty() {
        return this.isRotationDirty || this.dataTracker.isDirty();
    }

    public boolean isRotationDirty() {
        return isRotationDirty;
    }

    public void ignorePositionUpdates() {
        setSendPositionUpdates(false);
    }
    public void instantPositionUpdates() {
        setInstantPositionUpdates(true);
    }

    public void setInstantPositionUpdates(boolean value) {
        this.instantPositionUpdates = value;
    }

    public void setSendPositionUpdates(boolean b) {
        this.sendPositionUpdates = b;
    }

    public boolean isSendingPositionUpdates() {
        return this.sendPositionUpdates;
    }

    public void setAlwaysSyncAbsolutePosition(boolean alwaysSyncAbsolutePosition) {
        this.alwaysSyncAbsolutePosition = alwaysSyncAbsolutePosition;
    }

    public boolean isAlwaysSyncingAbsolutePosition() {
        return alwaysSyncAbsolutePosition;
    }

    public void setPitch(float pitch) {
        if (this.pitch != pitch) {
            this.pitch = pitch;
            this.isRotationDirty = true;
        }
    }

    public void setYaw(float yaw) {
        if (this.yaw != yaw) {
            this.yaw = yaw;
            this.isRotationDirty = true;
        }
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Override
    public IntList getEntityIds() {
        return IntList.of(id);
    }

    public final UUID getUuid() {
        return this.uuid;
    }

    public final int getEntityId() {
        return this.id;
    }

    protected abstract EntityType<? extends Entity> getEntityType();

    @Override
    public void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        if (!this.elementVisiblityPredicate.test(player)) {
            return;
        }

        packetConsumer.accept(this.createSpawnPacket(player));

        this.sendChangedTrackerEntries(player, packetConsumer);
    }

    protected Packet<ClientGamePacketListener> createSpawnPacket(ServerPlayer player) {
        if (this.lastSyncedPos == null) {
            this.lastSyncedPos = this.getCurrentPos();
        }
        return new ClientboundAddEntityPacket(this.id, this.uuid, this.lastSyncedPos.x, this.lastSyncedPos.y, this.lastSyncedPos.z, this.pitch, this.yaw, this.getEntityType(), 0, Vec3.ZERO, this.yaw);
    }

    protected void sendChangedTrackerEntries(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        var changed = this.dataTracker.getChangedEntries();

        if (changed != null) {
            packetConsumer.accept(new ClientboundSetEntityDataPacket(this.id, changed));
        }
    }

    @Override
    public void notifyMove(Vec3 oldPos, Vec3 newPos, Vec3 delta) {
        if (this.sendPositionUpdates && this.instantPositionUpdates) {
            this.sendPositionUpdates();
        }
    }

    @Override
    public void stopWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
    }

    @Override
    public void tick() {
        this.sendTrackerUpdates();
        if (this.sendPositionUpdates) {
            this.sendPositionUpdates();
        }
        this.sendRotationUpdates();
    }

    protected void sendPositionUpdates() {
        if (this.getHolder() == null) {
            return;
        }
        Packet<ClientGamePacketListener> packet = null;
        var pos = this.getCurrentPos();

        if (pos.equals(this.lastSyncedPos)) {
            return;
        }

        if (this.lastSyncedPos == null || this.alwaysSyncAbsolutePosition || this.updatesSinceLastAbsolutePositionSync > 200) {
            packet = new ClientboundEntityPositionSyncPacket(this.id, new PositionMoveRotation(pos, Vec3.ZERO, this.yaw, this.pitch), false);
            this.updatesSinceLastAbsolutePositionSync = 0;
        } else {
            packet = VirtualEntityUtils.createMovePacket(this.id, this.lastSyncedPos, pos, this.isRotationDirty, this.yaw, this.pitch);
            this.updatesSinceLastAbsolutePositionSync++;
        }

        if (packet != null) {
            this.sendPacket(packet);
            if (!(packet instanceof ClientboundMoveEntityPacket.Rot)) {
                this.lastSyncedPos = pos;
            }
        }
        this.isRotationDirty = false;
    }

    protected void sendTrackerUpdates() {
        if (this.dataTracker.isDirty()) {
            var dirty = this.dataTracker.getDirtyEntries();
            if (dirty != null) {
                this.sendPacket(new ClientboundSetEntityDataPacket(this.id, dirty));
            }
        }
    }

    protected void sendRotationUpdates() {
        if (this.isRotationDirty) {
            var i = Mth.floor(yaw * 256.0F / 360.0F);
            var j = Mth.floor(pitch * 256.0F / 360.0F);
            this.sendPacket(new ClientboundMoveEntityPacket.Rot(id, (byte) i, (byte) j, false));
            this.isRotationDirty = false;
        }
    }

    public SynchedEntityDataLike getDataTracker() {
        return this.dataTracker;
    }

    public Pose getPose() {
        return this.dataTracker.get(EntityData.POSE);
    }

    public void setPose(Pose pose) {
        this.dataTracker.set(EntityData.POSE, pose);
    }

    public void setOnFire(boolean onFire) {
        this.setFlag(EntityData.ON_FIRE_FLAG_INDEX, onFire);
    }

    protected boolean getFlag(int index) {
        return (this.dataTracker.get(EntityData.FLAGS) & 1 << index) != 0;
    }

    protected void setFlag(int index, boolean value) {
        byte b = this.dataTracker.get(EntityData.FLAGS);
        if (value) {
            this.dataTracker.set(EntityData.FLAGS, (byte) (b | 1 << index));
        } else {
            this.dataTracker.set(EntityData.FLAGS, (byte) (b & ~(1 << index)));
        }

    }

    public boolean isSneaking() {
        return this.getFlag(EntityData.SNEAKING_FLAG_INDEX);
    }

    public void setSneaking(boolean sneaking) {
        this.setFlag(EntityData.SNEAKING_FLAG_INDEX, sneaking);
    }

    public boolean isSprinting() {
        return this.getFlag(EntityData.SPRINTING_FLAG_INDEX);
    }

    public void setSprinting(boolean sprinting) {
        this.setFlag(EntityData.SPRINTING_FLAG_INDEX, sprinting);
    }

    public boolean isGlowing() {
        return this.getFlag(EntityData.GLOWING_FLAG_INDEX);
    }

    public final void setGlowing(boolean glowing) {
        this.setFlag(EntityData.GLOWING_FLAG_INDEX, glowing);
    }

    public boolean isInvisible() {
        return this.getFlag(EntityData.INVISIBLE_FLAG_INDEX);
    }

    public void setInvisible(boolean invisible) {
        this.setFlag(EntityData.INVISIBLE_FLAG_INDEX, invisible);
    }

    public int getAir() {
        return this.dataTracker.get(EntityData.AIR);
    }

    public void setAir(int air) {
        this.dataTracker.set(EntityData.AIR, air);
    }

    public int getFrozenTicks() {
        return this.dataTracker.get(EntityData.FROZEN_TICKS);
    }

    public void setFrozenTicks(int frozenTicks) {
        this.dataTracker.set(EntityData.FROZEN_TICKS, frozenTicks);
    }

    @Nullable
    public Component getCustomName() {
        return this.dataTracker.get(EntityData.CUSTOM_NAME).orElse(null);
    }

    public void setCustomName(@Nullable Component name) {
        this.dataTracker.set(EntityData.CUSTOM_NAME, Optional.ofNullable(name));
    }

    public boolean isCustomNameVisible() {
        return Boolean.TRUE == this.dataTracker.get(EntityData.NAME_VISIBLE);
    }

    public void setCustomNameVisible(boolean visible) {
        this.dataTracker.set(EntityData.NAME_VISIBLE, visible);
    }

    public boolean isSilent() {
        return Boolean.TRUE == this.dataTracker.get(EntityData.SILENT);
    }

    public void setSilent(boolean silent) {
        this.dataTracker.set(EntityData.SILENT, silent);
    }

    public boolean hasNoGravity() {
        return Boolean.TRUE == this.dataTracker.get(EntityData.NO_GRAVITY);
    }

    public void setNoGravity(boolean noGravity) {
        this.dataTracker.set(EntityData.NO_GRAVITY, noGravity);
    }

    public void setRotation(float pitch, float yaw) {
        this.setPitch(pitch);
        this.setYaw(yaw);
    }
}
