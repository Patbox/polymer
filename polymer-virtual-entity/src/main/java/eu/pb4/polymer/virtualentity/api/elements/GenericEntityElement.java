package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.tracker.DataTrackerLike;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.api.tracker.SimpleDataTracker;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("ConstantConditions")
public abstract class GenericEntityElement extends AbstractElement {
    protected final DataTrackerLike dataTracker = this.createDataTracker();
    private final int id = VirtualEntityUtils.requestEntityId();
    private final UUID uuid = UUID.randomUUID();
    private float pitch;
    private float yaw;
    private boolean isRotationDirty;
    private boolean sendPositionUpdates = true;

    protected DataTrackerLike createDataTracker() {
        return new SimpleDataTracker(this.getEntityType());
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

    public void setSendPositionUpdates(boolean b) {
        this.sendPositionUpdates = b;
    }

    public boolean isSendingPositionUpdates() {
        return this.sendPositionUpdates;
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


    public void setOffset(Vec3d offset) {
        super.setOffset(offset);
        if (this.sendPositionUpdates && this.getHolder() != null) {
            var x = VirtualEntityUtils.createMovePacket(this.id, this.getHolder().getPos().add(this.getOffset()), this.getHolder().getPos().add(offset), false, 0f, 0f);
            if (x != null) {
                this.getHolder().sendPacket(x);
            }
        }
    }

    protected abstract EntityType<? extends Entity> getEntityType();

    @Override
    public void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        packetConsumer.accept(this.createSpawnPacket(player));

        this.sendChangedTrackerEntries(player, packetConsumer);
    }

    protected Packet<ClientPlayPacketListener> createSpawnPacket(ServerPlayerEntity player) {
        var pos = this.getHolder().getPos().add(this.getOffset());
        return new EntitySpawnS2CPacket(this.id, this.uuid, pos.x, pos.y, pos.z, this.pitch, this.yaw, this.getEntityType(), 0, Vec3d.ZERO, this.yaw);
    }

    protected void sendChangedTrackerEntries(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
        var changed = this.dataTracker.getChangedEntries();

        if (changed != null) {
            packetConsumer.accept(new EntityTrackerUpdateS2CPacket(this.id, changed));
        }
    }

    @Override
    public void notifyMove(Vec3d oldPos, Vec3d newPos, Vec3d delta) {
        if (this.sendPositionUpdates) {
            var x = VirtualEntityUtils.createMovePacket(this.id, oldPos.add(this.getOffset()), newPos.add(this.getOffset()), this.isRotationDirty, this.yaw, this.pitch);
            if (x != null) {
                this.getHolder().sendPacket(x);
            }
            this.isRotationDirty = false;
        }
    }

    @Override
    public void stopWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {
    }

    @Override
    public void tick() {
        this.sendTrackerUpdates();
        this.sendRotationUpdates();
    }

    protected void sendTrackerUpdates() {
        if (this.dataTracker.isDirty()) {
            var dirty = this.dataTracker.getDirtyEntries();
            if (dirty != null) {
                this.getHolder().sendPacket(new EntityTrackerUpdateS2CPacket(this.id, dirty));
            }
        }
    }

    protected void sendRotationUpdates() {
        if (this.isRotationDirty) {
            var i = MathHelper.floor(yaw * 256.0F / 360.0F);
            var j = MathHelper.floor(pitch * 256.0F / 360.0F);
            this.getHolder().sendPacket(new EntityS2CPacket.Rotate(id, (byte) i, (byte) j, false));
            this.isRotationDirty = false;
        }
    }

    public DataTrackerLike getDataTracker() {
        return this.dataTracker;
    }

    public EntityPose getPose() {
        return this.dataTracker.get(EntityTrackedData.POSE);
    }

    public void setPose(EntityPose pose) {
        this.dataTracker.set(EntityTrackedData.POSE, pose);
    }

    public void setOnFire(boolean onFire) {
        this.setFlag(EntityTrackedData.ON_FIRE_FLAG_INDEX, onFire);
    }

    protected boolean getFlag(int index) {
        return (this.dataTracker.get(EntityTrackedData.FLAGS) & 1 << index) != 0;
    }

    protected void setFlag(int index, boolean value) {
        byte b = this.dataTracker.get(EntityTrackedData.FLAGS);
        if (value) {
            this.dataTracker.set(EntityTrackedData.FLAGS, (byte) (b | 1 << index));
        } else {
            this.dataTracker.set(EntityTrackedData.FLAGS, (byte) (b & ~(1 << index)));
        }

    }

    public boolean isSneaking() {
        return this.getFlag(EntityTrackedData.SNEAKING_FLAG_INDEX);
    }

    public void setSneaking(boolean sneaking) {
        this.setFlag(EntityTrackedData.SNEAKING_FLAG_INDEX, sneaking);
    }

    public boolean isSprinting() {
        return this.getFlag(EntityTrackedData.SPRINTING_FLAG_INDEX);
    }

    public void setSprinting(boolean sprinting) {
        this.setFlag(EntityTrackedData.SPRINTING_FLAG_INDEX, sprinting);
    }

    public boolean isGlowing() {
        return this.getFlag(EntityTrackedData.GLOWING_FLAG_INDEX);
    }

    public final void setGlowing(boolean glowing) {
        this.setFlag(EntityTrackedData.GLOWING_FLAG_INDEX, glowing);
    }

    public boolean isInvisible() {
        return this.getFlag(EntityTrackedData.INVISIBLE_FLAG_INDEX);
    }

    public void setInvisible(boolean invisible) {
        this.setFlag(EntityTrackedData.INVISIBLE_FLAG_INDEX, invisible);
    }

    public int getAir() {
        return this.dataTracker.get(EntityTrackedData.AIR);
    }

    public void setAir(int air) {
        this.dataTracker.set(EntityTrackedData.AIR, air);
    }

    public int getFrozenTicks() {
        return this.dataTracker.get(EntityTrackedData.FROZEN_TICKS);
    }

    public void setFrozenTicks(int frozenTicks) {
        this.dataTracker.set(EntityTrackedData.FROZEN_TICKS, frozenTicks);
    }

    @Nullable
    public Text getCustomName() {
        return this.dataTracker.get(EntityTrackedData.CUSTOM_NAME).orElse(null);
    }

    public void setCustomName(@Nullable Text name) {
        this.dataTracker.set(EntityTrackedData.CUSTOM_NAME, Optional.ofNullable(name));
    }

    public boolean isCustomNameVisible() {
        return Boolean.TRUE == this.dataTracker.get(EntityTrackedData.NAME_VISIBLE);
    }

    public void setCustomNameVisible(boolean visible) {
        this.dataTracker.set(EntityTrackedData.NAME_VISIBLE, visible);
    }

    public boolean isSilent() {
        return Boolean.TRUE == this.dataTracker.get(EntityTrackedData.SILENT);
    }

    public void setSilent(boolean silent) {
        this.dataTracker.set(EntityTrackedData.SILENT, silent);
    }

    public boolean hasNoGravity() {
        return Boolean.TRUE == this.dataTracker.get(EntityTrackedData.NO_GRAVITY);
    }

    public void setNoGravity(boolean noGravity) {
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, noGravity);
    }
}
