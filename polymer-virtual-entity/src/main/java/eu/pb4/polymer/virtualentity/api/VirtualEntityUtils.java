package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.virtualentity.mixin.EntityPassengersSetS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.SetCameraEntityS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityPositionS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.PlaySoundFromEntityS2CPacketAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;


public final class VirtualEntityUtils {
    private VirtualEntityUtils() {}
    public static int requestEntityId() {
        return EntityAccessor.getCURRENT_ID().incrementAndGet();
    }

    public static void addVirtualPassenger(Entity entity, int passengerId) {
        ((EntityExt) entity).polymerVE$getVirtualRidden().add(passengerId);
        ((EntityExt) entity).polymerVE$markVirtualRiddenDirty();
    }

    public static void addVirtualPassenger(Entity entity, int... passengerId) {
        for (var i : passengerId) {
            ((EntityExt) entity).polymerVE$getVirtualRidden().add(i);
        }
        ((EntityExt) entity).polymerVE$markVirtualRiddenDirty();
    }

    public static void removeVirtualPassenger(Entity entity, int passengerId) {
        ((EntityExt) entity).polymerVE$getVirtualRidden().rem(passengerId);
        ((EntityExt) entity).polymerVE$markVirtualRiddenDirty();
    }

    public static void removeVirtualPassenger(Entity entity, int... passengerId) {
        for (var i : passengerId) {
            ((EntityExt) entity).polymerVE$getVirtualRidden().rem(i);
        }
        ((EntityExt) entity).polymerVE$markVirtualRiddenDirty();
    }

    public static SetCameraEntityS2CPacket createSetCameraEntityPacket(int entityId) {
        var packet = PolymerCommonUtils.createUnsafe(SetCameraEntityS2CPacket.class);
        var ac = (SetCameraEntityS2CPacketAccessor) packet;
        ac.setEntityId(entityId);
        return packet;
    }

    public static PlaySoundFromEntityS2CPacket createPlaySoundFromEntityPacket(int entityId, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {
        var packet = PolymerCommonUtils.createUnsafe(PlaySoundFromEntityS2CPacket.class);
        var ac = (PlaySoundFromEntityS2CPacketAccessor) packet;
        ac.setEntityId(entityId);
        ac.setSound(sound);
        ac.setCategory(category);
        ac.setVolume(volume);
        ac.setPitch(pitch);
        ac.setSeed(seed);
        return packet;
    }


    @Nullable
    public static Packet<ClientPlayPacketListener> createMovePacket(int id, Vec3d oldPos, Vec3d newPos, boolean rotate, float yaw, float pitch) {
        var byteYaw = MathHelper.floor(yaw * 256.0F / 360.0F);
        var bytePitch = MathHelper.floor(pitch * 256.0F / 360.0F);
        boolean areDifferentEnough = oldPos.subtract(newPos).lengthSquared() >= 7.62939453125E-6D;
        long newX = Math.round((newPos.x - oldPos.x) * 4096.0D);
        long newY = Math.round((newPos.y - oldPos.y) * 4096.0D);
        long newZ = Math.round((newPos.z - oldPos.z) * 4096.0D);
        boolean bl5 = newX < -32768L || newX > 32767L || newY < -32768L || newY > 32767L || newZ < -32768L || newZ > 32767L;
        if (!bl5) {
            if ((!areDifferentEnough || !rotate)) {
                if (areDifferentEnough) {
                    return new EntityS2CPacket.MoveRelative(id, (short) ((int) newX), (short) ((int) newY), (short) ((int) newZ), false);
                } else if (rotate) {
                    return new EntityS2CPacket.Rotate(id, (byte) byteYaw, (byte) bytePitch, false);
                }
            } else {
                return new EntityS2CPacket.RotateAndMoveRelative(id, (short) ((int) newX), (short) ((int) newY), (short) ((int) newZ), (byte) byteYaw, (byte) bytePitch, false);
            }

            return null;
        } else {
            return createSimpleMovePacket(id, newPos, (byte) byteYaw, (byte) bytePitch);
        }
    }

    public static Packet<ClientPlayPacketListener> createSimpleMovePacket(int id, Vec3d newPos, byte yaw, byte pitch) {
        var packet = PolymerCommonUtils.createUnsafe(EntityPositionS2CPacket.class);
        var accessor = (EntityPositionS2CPacketAccessor) packet;
        accessor.setId(id);
        accessor.setX(newPos.x);
        accessor.setY(newPos.y);
        accessor.setZ(newPos.z);
        accessor.setOnGround(false);
        accessor.setPitch(pitch);
        accessor.setYaw(yaw);
        return packet;
    }

    public static EntityPassengersSetS2CPacket createRidePacket(int id, IntList list) {
        return createRidePacket(id, list.toIntArray());
    }

    public static EntityPassengersSetS2CPacket createRidePacket(int id, int[] list) {
        var packet = PolymerCommonUtils.createUnsafe(EntityPassengersSetS2CPacket.class);
        ((EntityPassengersSetS2CPacketAccessor) packet).setEntityId(id);
        ((EntityPassengersSetS2CPacketAccessor) packet).setPassengerIds(list);
        return packet;
    }

    public static boolean isPlayerTracking(ServerPlayerEntity player, WorldChunk chunk) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            return ImmersivePortalsUtils.isPlayerTracking(player, chunk);
        }

        if (player.getWorld() != chunk.getWorld()) {
            return false;
        }

        return player.getChunkFilter().isWithinDistance(chunk.getPos().x, chunk.getPos().z);
    }

    /**
     * Purely for compatibility with immersive portals.
     */
    public static void wrapCallWithContext(ServerWorld world, Runnable call) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            ImmersivePortalsUtils.callRedirected(world, call);
        } else {
            call.run();
        }
    }
}
