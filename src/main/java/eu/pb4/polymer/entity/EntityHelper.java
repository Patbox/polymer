package eu.pb4.polymer.entity;

import eu.pb4.polymer.mixin.entity.PlayerSpawnS2CPacketAccessor;
import eu.pb4.polymer.other.InternalHelpers;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class EntityHelper {
    private static final HashSet<EntityType<?>> ENTITY_IDENTIFIERS = new HashSet<>();

    /**
     * Marks EntityTypes as server-side only
     *
     * @param types Entity Types
     */
    public static void registerVirtualEntityType(EntityType<?>... types) {
        ENTITY_IDENTIFIERS.addAll(Arrays.asList(types));
    }

    /**
     * Checks if EntityType is server-side only
     *
     * @param type EntityType
     */
    public static boolean isVirtualEntityType(EntityType<?> type) {
        return ENTITY_IDENTIFIERS.contains(type);
    }


    /**
     * @param type EntityType
     * @return List of default DataTracker entries for entity type
     */
    public static List<DataTracker.Entry<?>> getDefaultDataTrackerEntries(EntityType<?> type) {
        return InternalHelpers.getExampleTrackedDataOfEntityType(type);
    }

    /**
     * @param type EntityType
     * @return Entity Class associated with EntityType
     */
    public static <T extends Entity> Class<T> getEntityClass(EntityType<T> type) {
        return InternalHelpers.getEntityClass(type);
    }

    /**
     * @param type EntityType
     * @return True if EntityType is LivingEntity;
     */
    public static boolean isLivingEntity(EntityType<?> type) {
        return InternalHelpers.isLivingEntity(type);
    }

    /**
     * @param type EntityType
     * @return True if EntityType is MobEntity;
     */
    public static boolean isMobEntity(EntityType<?> type) {
        return InternalHelpers.isMobEntity(type);
    }

    /**
     * @return Creates PlayerEntity spawn packet, that can be used by VirtualEntities
     */
    @Nullable
    public static PlayerSpawnS2CPacket createPlayerSpawnPacket(int entityId, UUID uuid, double x, double y, double z, float yaw, float pitch) {
        try {
            PlayerSpawnS2CPacket packet = (PlayerSpawnS2CPacket) UnsafeAccess.UNSAFE.allocateInstance(PlayerSpawnS2CPacket.class);
            var accessor = (PlayerSpawnS2CPacketAccessor) packet;
            accessor.setId(entityId);
            accessor.setUuid(uuid);
            accessor.setYaw((byte) ((int) (yaw * 256.0F / 360.0F)));
            accessor.setPitch((byte) ((int) (pitch * 256.0F / 360.0F)));
            accessor.setX(x);
            accessor.setY(y);
            accessor.setZ(z);
            return packet;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return Creates PlayerEntity spawn packet, that can be used by VirtualEntities
     */
    @Nullable
    public static PlayerSpawnS2CPacket createPlayerSpawnPacket(Entity entity) {
        return createPlayerSpawnPacket(entity.getId(), entity.getUuid(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }
}
