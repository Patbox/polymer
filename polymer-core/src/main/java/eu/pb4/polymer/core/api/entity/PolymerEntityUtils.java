package eu.pb4.polymer.core.api.entity;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.mixin.entity.EntityAccessor;
import eu.pb4.polymer.core.mixin.entity.PlayerListS2CPacketAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PolymerEntityUtils {
    private PolymerEntityUtils() {
    }

    private static final Set<EntityType<?>> ENTITY_TYPES = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
    private static final Set<EntityAttribute> ENTITY_ATTRIBUTES = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);

    private static final Map<VillagerProfession, PolymerVillagerProfession> VILLAGER_PROFESSIONS = new Object2ObjectOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);

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
     * Marks EntityAttribute as server-side only
     */
    @SafeVarargs
    public static void registerAttribute(RegistryEntry<EntityAttribute>... attributes) {
        for (var type : attributes) {
            ENTITY_ATTRIBUTES.add(type.value());
            RegistrySyncUtils.setServerEntry(Registries.ATTRIBUTE, type.value());
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
    public static boolean isPolymerEntityType(EntityType<?> type) {
        return ENTITY_TYPES.contains(type);
    }

    public static boolean isPolymerEntityAttribute(RegistryEntry<EntityAttribute> type) {
        return ENTITY_ATTRIBUTES.contains(type.value());
    }

    /**
     * @param type EntityType
     * @return Array of default DataTracker entries for entity type
     */
    public static DataTracker.Entry<?>[] getDefaultTrackedData(EntityType<?> type) {
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
    public static PlayerListS2CPacket createMutablePlayerListPacket(EnumSet<PlayerListS2CPacket.Action> actions) {
        var packet = new PlayerListS2CPacket(actions, List.of());
        ((PlayerListS2CPacketAccessor) packet).setEntries(new ArrayList<>());
        return packet;
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

