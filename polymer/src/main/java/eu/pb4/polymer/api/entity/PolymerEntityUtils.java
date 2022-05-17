package eu.pb4.polymer.api.entity;

import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.compat.QuiltRegistryUtils;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.mixin.entity.EntityAccessor;
import eu.pb4.polymer.mixin.entity.PlayerSpawnS2CPacketAccessor;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PolymerEntityUtils {
    private PolymerEntityUtils() {
    }

    private static final Set<EntityType<?>> ENTITY_TYPES = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());

    /**
     * Allows to get next free entity id you can use for networking
     *
     * @return free entity id
     */
    public static int requestFreeId() {
        return EntityAccessor.getCURRENT_ID().incrementAndGet();
    }

    /**
     * Marks EntityTypes as server-side only
     *
     * @param types Entity Types
     */
    public static void registerType(EntityType<?>... types) {
        ENTITY_TYPES.addAll(Arrays.asList(types));

        if (CompatStatus.QUILT_REGISTRY) {
            for (var type : types) {
                QuiltRegistryUtils.markAsOptional(Registry.ENTITY_TYPE, type);
            }
        }

        var reg = (RegistryExtension) Registry.ENTITY_TYPE;
        if (reg.polymer_getStatus() == RegistryExtension.Status.WITH_REGULAR_MODS) {
            reg.polymer_setStatus(RegistryExtension.Status.VANILLA_ONLY);
            for (var entry : Registry.ENTITY_TYPE.getEntrySet()) {
                if (entry.getKey().getValue().getNamespace().equals("minecraft")) {
                    continue;
                }

                if (ENTITY_TYPES.contains(entry.getValue())) {
                    reg.polymer_updateStatus(RegistryExtension.Status.WITH_POLYMER);
                } else {
                    reg.polymer_updateStatus(RegistryExtension.Status.WITH_REGULAR_MODS);
                    return;
                }
            }
        }
    }

    /**
     * Checks if EntityType is server-side only
     *
     * @param type EntityType
     */
    public static boolean isRegisteredEntityType(EntityType<?> type) {
        return ENTITY_TYPES.contains(type);
    }


    /**
     * @param type EntityType
     * @return List of default DataTracker entries for entity type
     */
    public static List<DataTracker.Entry<?>> getDefaultDataTrackerEntries(EntityType<?> type) {
        return InternalEntityHelpers.getExampleTrackedDataOfEntityType(type);
    }

    /**
     * @param type EntityType
     * @return Entity Class associated with EntityType
     */
    public static <T extends Entity> Class<T> getEntityClass(EntityType<T> type) {
        return InternalEntityHelpers.getEntityClass(type);
    }

    /**
     * @param type EntityType
     * @return True if EntityType is LivingEntity;
     */
    public static boolean isLivingEntity(EntityType<?> type) {
        return InternalEntityHelpers.isLivingEntity(type);
    }

    /**
     * @param type EntityType
     * @return True if EntityType is MobEntity;
     */
    public static boolean isMobEntity(EntityType<?> type) {
        return InternalEntityHelpers.isMobEntity(type);
    }

    /**
     * @return Creates PlayerEntity spawn packet, that can be used by VirtualEntities
     */
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
    public static PlayerSpawnS2CPacket createPlayerSpawnPacket(Entity entity) {
        return createPlayerSpawnPacket(entity.getId(), entity.getUuid(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
    }
}
