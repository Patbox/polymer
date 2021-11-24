package eu.pb4.polymer.entity;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.mixin.entity.PlayerSpawnS2CPacketAccessor;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Use {@link eu.pb4.polymer.api.entity.PolymerEntityUtils} instead
 */
@Deprecated
public class EntityHelper {

    public static void registerVirtualEntityType(EntityType<?>... types) {
        PolymerEntityUtils.registerType(types);
    }

    public static boolean isVirtualEntityType(EntityType<?> type) {
        return PolymerEntityUtils.isRegisteredEntityType(type);
    }


    public static List<DataTracker.Entry<?>> getDefaultDataTrackerEntries(EntityType<?> type) {
        return PolymerEntityUtils.getDefaultDataTrackerEntries(type);
    }

    public static <T extends Entity> Class<T> getEntityClass(EntityType<T> type) {
        return PolymerEntityUtils.getEntityClass(type);
    }

    public static boolean isLivingEntity(EntityType<?> type) {
        return PolymerEntityUtils.isLivingEntity(type);
    }

    public static boolean isMobEntity(EntityType<?> type) {
        return PolymerEntityUtils.isMobEntity(type);
    }

    @Nullable
    public static PlayerSpawnS2CPacket createPlayerSpawnPacket(int entityId, UUID uuid, double x, double y, double z, float yaw, float pitch) {
        return PolymerEntityUtils.createPlayerSpawnPacket(entityId, uuid, x, y, z, yaw, pitch);
    }

    @Nullable
    public static PlayerSpawnS2CPacket createPlayerSpawnPacket(Entity entity) {
        return createPlayerSpawnPacket(entity.getId(), entity.getUuid(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }
}
