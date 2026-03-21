package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.virtualentity.mixin.EntityPassengersSetS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.SetCameraEntityS2CPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ClientboundSetEntityLinkPacketAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ClientboundSoundEntityPacketAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;


public final class VirtualEntityUtils {
    private VirtualEntityUtils() {}
    public static int requestEntityId() {
        return EntityAccessor.getENTITY_COUNTER().incrementAndGet();
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

    public static ClientboundSetEntityLinkPacket createClientboundSetEntityLinkPacket(int attachedId, int holdingId) {
        var packet = PolymerCommonUtils.createUnsafe(ClientboundSetEntityLinkPacket.class);
        var ac = (ClientboundSetEntityLinkPacketAccessor) packet;
        ac.setSourceId(attachedId);
        ac.setDestId(holdingId);
        return packet;
    }
    public static ClientboundSetCameraPacket createClientboundSetCameraPacket(int entityId) {
        var packet = PolymerCommonUtils.createUnsafe(ClientboundSetCameraPacket.class);
        var ac = (SetCameraEntityS2CPacketAccessor) packet;
        ac.setCameraId(entityId);
        return packet;
    }

    public static ClientboundSoundEntityPacket createClientboundSoundEntityPacket(int entityId, Holder<SoundEvent> sound, SoundSource category, float volume, float pitch, long seed) {
        var packet = PolymerCommonUtils.createUnsafe(ClientboundSoundEntityPacket.class);
        var ac = (ClientboundSoundEntityPacketAccessor) packet;
        ac.setId(entityId);
        ac.setSound(sound);
        ac.setSource(category);
        ac.setVolume(volume);
        ac.setPitch(pitch);
        ac.setSeed(seed);
        return packet;
    }


    @Nullable
    public static Packet<ClientGamePacketListener> createMovePacket(int id, Vec3 oldPos, Vec3 newPos, boolean rotate, float yaw, float pitch) {
        var byteYaw = Mth.floor(yaw * 256.0F / 360.0F);
        var bytePitch = Mth.floor(pitch * 256.0F / 360.0F);
        boolean areDifferentEnough = oldPos.subtract(newPos).lengthSqr() >= 7.62939453125E-6D;
        long newX = Math.round((newPos.x - oldPos.x) * 4096.0D);
        long newY = Math.round((newPos.y - oldPos.y) * 4096.0D);
        long newZ = Math.round((newPos.z - oldPos.z) * 4096.0D);
        boolean bl5 = newX < -32768L || newX > 32767L || newY < -32768L || newY > 32767L || newZ < -32768L || newZ > 32767L;
        if (!bl5) {
            if ((!areDifferentEnough || !rotate)) {
                if (areDifferentEnough) {
                    return new ClientboundMoveEntityPacket.Pos(id, (short) ((int) newX), (short) ((int) newY), (short) ((int) newZ), false);
                } else if (rotate) {
                    return new ClientboundMoveEntityPacket.Rot(id, (byte) byteYaw, (byte) bytePitch, false);
                }
            } else {
                return new ClientboundMoveEntityPacket.PosRot(id, (short) ((int) newX), (short) ((int) newY), (short) ((int) newZ), (byte) byteYaw, (byte) bytePitch, false);
            }

            return null;
        } else {
            return new ClientboundEntityPositionSyncPacket(id, new PositionMoveRotation(newPos, Vec3.ZERO, yaw, pitch), false);
        }
    }

    public static ClientboundSetPassengersPacket createClientboundSetPassengersPacket(int id, IntList list) {
        return createClientboundSetPassengersPacket(id, list.toIntArray());
    }

    public static ClientboundSetPassengersPacket createClientboundSetPassengersPacket(int id, int[] list) {
        var packet = PolymerCommonUtils.createUnsafe(ClientboundSetPassengersPacket.class);
        ((EntityPassengersSetS2CPacketAccessor) packet).setVehicle(id);
        ((EntityPassengersSetS2CPacketAccessor) packet).setPassengers(list);
        return packet;
    }

    public static boolean isPlayerTracking(ServerPlayer player, LevelChunk chunk) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            return ImmersivePortalsUtils.isPlayerTracking(player, chunk);
        }

        if (player.level() != chunk.getLevel()) {
            return false;
        }

        return player.getChunkTrackingView().contains(chunk.getPos().x(), chunk.getPos().z());
    }

    /**
     * Purely for compatibility with immersive portals.
     */
    public static void wrapCallWithContext(ServerLevel world, Runnable call) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            ImmersivePortalsUtils.callRedirected(world, call);
        } else {
            call.run();
        }
    }
}
