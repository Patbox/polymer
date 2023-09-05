package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.VirtualEntityImplUtils;
import eu.pb4.polymer.virtualentity.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.virtualentity.mixin.EntityPassengersSetS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityPositionS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ThreadedAnvilChunkStorageAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
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

    @Nullable
    public static Packet<ClientPlayPacketListener> createMovePacket(int id, Vec3d oldPos, Vec3d newPos, boolean rotate, float yaw, float pitch) {
        var i = MathHelper.floor(yaw * 256.0F / 360.0F);
        var j = MathHelper.floor(pitch * 256.0F / 360.0F);
        boolean bl2 = oldPos.subtract(newPos).lengthSquared() >= 7.62939453125E-6D;
        long l = Math.round(newPos.x * 4096.0D);
        long m = Math.round(newPos.y * 4096.0D);
        long n = Math.round(newPos.z * 4096.0D);
        boolean bl5 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
        if (!bl5) {
            if ((!bl2 || !rotate)) {
                if (bl2) {
                    return new EntityS2CPacket.MoveRelative(id, (short) ((int) l), (short) ((int) m), (short) ((int) n), false);
                } else if (rotate) {
                    return new EntityS2CPacket.Rotate(id, (byte) i, (byte) j, false);
                }
            } else {
                return new EntityS2CPacket.RotateAndMoveRelative(id, (short) ((int) l), (short) ((int) m), (short) ((int) n), (byte) i, (byte) j, false);
            }

            return null;
        } else {
            return createSimpleMovePacket(id, newPos, (byte) i, (byte) j);
        }
    }

    public static Packet<ClientPlayPacketListener> createSimpleMovePacket(int id, Vec3d newPos, byte yaw, byte pitch) {
        var packet = VirtualEntityImplUtils.createUnsafe(EntityPositionS2CPacket.class);
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
        var packet = VirtualEntityImplUtils.createUnsafe(EntityPassengersSetS2CPacket.class);
        ((EntityPassengersSetS2CPacketAccessor) packet).setId(id);
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
