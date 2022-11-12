package eu.pb4.polymer.api.entity;

import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.mixin.entity.EntityAccessor;
import eu.pb4.polymer.mixin.entity.PlayerSpawnS2CPacketAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registries;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PolymerEntityUtils {
    private PolymerEntityUtils() {
    }

    private static final Set<EntityType<?>> ENTITY_TYPES = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());

    private static final Map<VillagerProfession, PolymerVillagerProfession> VILLAGER_PROFESSIONS = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());

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

        for (var type : types) {
            RegistrySyncUtils.setServerEntry(Registries.ENTITY_TYPE, type);
        }
    }

    /**
     * Marks EntityTypes as server-side only
     *
     * @param profession VillagerProfession to server side
     * @param mapper object managing mapping to client compatible one
     */
    public static void registerProfession(VillagerProfession profession, PolymerVillagerProfession mapper) {
        VILLAGER_PROFESSIONS.put(profession, mapper);
        RegistrySyncUtils.setServerEntry(Registries.VILLAGER_PROFESSION, profession);
    }

    @Nullable
    public static PolymerVillagerProfession getPolymerProfession(VillagerProfession profession) {
        return VILLAGER_PROFESSIONS.get(profession);
    }

    /**
     * Checks if EntityType is server-side only
     *
     * @param type EntityType
     */
    public static boolean isRegisteredEntityType(EntityType<?> type) {
        return ENTITY_TYPES.contains(type);
    }
    /*
    / **
     * @param type EntityType
     * @return List of default DataTracker entries for entity type
     * /
    public static List<DataTracker.Entry<?>> getDefaultDataTrackerEntries(EntityType<?> type) {
        return InternalEntityHelpers.getExampleTrackedDataOfEntityType(type);
    }*/

    /**
     * @param type EntityType
     * @return List of default DataTracker entries for entity type
     */
    public static Int2ObjectMap<DataTracker.Entry<?>> getDefaultTrackedData(EntityType<?> type) {
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

    public static boolean canHoldEntityContext(Packet<?> packet) {
        return packet instanceof EntityAttachedPacket;
    }

    public static <T extends Packet<ClientPlayPacketListener>> T setEntityContext(T packet, Entity entity) {
        return EntityAttachedPacket.setIfEmpty(packet, entity);
    }

    public static <T extends Packet<ClientPlayPacketListener>> T forceSetEntityContext(T packet, Entity entity) {
        return EntityAttachedPacket.set(packet, entity);
    }

    @Nullable
    public static Entity getEntityContext(Packet<?> packet) {
        return EntityAttachedPacket.get(packet);
    }

    public static void sendEntityType(ServerPlayerEntity player, int entityId, EntityType<?> entityType) {
        PolymerServerProtocol.sendEntityInfo(player.networkHandler, entityId, entityType);
    }
}
