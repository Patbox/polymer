package eu.pb4.polymer.core.api.entity;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.entity.OneOfPolymerEntityConstructors;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.PolymerEntityProvider;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.mixin.block.packet.ServerMapAccessor;
import eu.pb4.polymer.core.mixin.entity.EntityAccessor;
import eu.pb4.polymer.core.mixin.entity.TrackedEntityAccessor;
import eu.pb4.polymer.core.mixin.entity.ClientboundPlayerInfoUpdatePacketAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;

public final class PolymerEntityUtils {
    private PolymerEntityUtils() {
    }
    public static final Event<PolymerEntityInteractionListener> POLYMER_ENTITY_INTERACTION_CHECK = EventFactory.createArrayBacked(PolymerEntityInteractionListener.class,
            arr -> (player, hand, stack, world, entity, actionResult) -> {
                for (var c : arr) {
                    if (c.isPolymerEntityInteraction(player, hand, stack, world, entity, actionResult)) {
                        return true;
                    }
                }

                return false;
            });

    private static final Map<EntityType<?>, Function<Entity, PolymerEntity>> POLYMER_ENTITY_CONSTRUCTORS = new IdentityHashMap<>();
    private static final Set<Attribute> ENTITY_ATTRIBUTES = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);

    /**
     * Allows to get next free entity id you can use for networking
     *
     * @return free entity id
     */
    public static int requestFreeId() {
        return EntityAccessor.getENTITY_COUNTER().incrementAndGet();
    }

    /**
     * Marks EntityTypes as server-side only
     *
     * @param types Entity Types
     */
    public static void registerType(EntityType<?>... types) {
        for (var type : types) {
            registerPolymerEntityConstructor(type, entity -> entity instanceof PolymerEntity polymerEntity ? polymerEntity : null);
        }

        for (var type : types) {
            PolymerSyncedObject.setSyncedObject(BuiltInRegistries.ENTITY_TYPE, type, (ent, ctx) -> EntityType.MARKER);
        }
    }

    public static void registerType(EntityType<?> type, PolymerSyncedObject<EntityType<?>> syncedObject) {
        registerPolymerEntityConstructor(type, entity -> entity instanceof PolymerEntity polymerEntity ? polymerEntity : (context -> syncedObject.getPolymerReplacement(((Entity) entity).getType(), context)));
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.ENTITY_TYPE, type, syncedObject);
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, Function<T, PolymerEntity> constructor) {
        registerPolymerEntityConstructor(type, constructor);
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.ENTITY_TYPE, type, (ent, ctx) -> EntityType.MARKER);
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, PolymerSyncedObject<EntityType<?>> syncedObject, Function<T, PolymerEntity> constructor) {
        //noinspection unchecked
        registerPolymerEntityConstructor(type, constructor);
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.ENTITY_TYPE, type, syncedObject);
    }

    public static <T extends Entity> void registerPolymerEntityConstructor(EntityType<T> type, Function<T, @Nullable PolymerEntity> constructor) {
        if (POLYMER_ENTITY_CONSTRUCTORS.containsKey(type)) {
            var old = POLYMER_ENTITY_CONSTRUCTORS.get(type);
            //noinspection rawtypes,unchecked
            POLYMER_ENTITY_CONSTRUCTORS.put(type, new OneOfPolymerEntityConstructors(constructor, old));
        } else {
            //noinspection unchecked
            POLYMER_ENTITY_CONSTRUCTORS.put(type, (Function<Entity, PolymerEntity>) constructor);
        }
    }

    @Nullable
    public static <T extends Entity> Function<T, @Nullable PolymerEntity> getPolymerEntityConstructor(EntityType<T> type) {
        //noinspection unchecked
        return (Function<T, PolymerEntity>) POLYMER_ENTITY_CONSTRUCTORS.get(type);
    }

    /**
     * Marks EntityAttribute as server-side only
     */
    @SafeVarargs
    public static void registerAttribute(Holder<Attribute>... attributes) {
        for (var type : attributes) {
            ENTITY_ATTRIBUTES.add(type.value());
            RegistrySyncUtils.setServerEntry(BuiltInRegistries.ATTRIBUTE, type.value());
        }
    }

    /**
     * Marks EntityTypes as server-side only
     *
     * @param profession VillagerProfession to server side
     * @param mapper object managing mapping to client compatible one
     */
    public static void registerProfession(VillagerProfession profession, PolymerSyncedObject<VillagerProfession> mapper) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.VILLAGER_PROFESSION, profession, mapper);
    }

    @Nullable
    public static PolymerSyncedObject<VillagerProfession> getPolymerProfession(VillagerProfession profession) {
        return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.VILLAGER_PROFESSION, profession);
    }

    /**
     * Checks if EntityType is server-side only
     *
     * @param type EntityType
     */
    public static boolean isPolymerEntityType(EntityType<?> type) {
        return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ENTITY_TYPE, type) != null;
    }

    public static boolean isPolymerAttribute(Holder<Attribute> type) {
        return ENTITY_ATTRIBUTES.contains(type.value());
    }

    /**
     * @param type EntityType
     * @return Array of default DataTracker entries for entity type
     */
    public static SynchedEntityData.DataItem<?>[] getDefaultSynchedEntityData(EntityType<?> type) {
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
    public static ClientboundPlayerInfoUpdatePacket createMutablePlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions) {
        var packet = new ClientboundPlayerInfoUpdatePacket(actions, List.of());
        ((ClientboundPlayerInfoUpdatePacketAccessor) packet).setEntries(new ArrayList<>());
        return packet;
    }

    public static boolean canHoldEntityContext(Packet<?> packet) {
        return packet instanceof EntityAttachedPacket;
    }

    public static <T extends Packet<ClientGamePacketListener>> T setEntityContext(T packet, Entity entity) {
        return EntityAttachedPacket.setIfEmpty(packet, entity);
    }

    public static <T extends Packet<ClientGamePacketListener>> T forceSetEntityContext(T packet, Entity entity) {
        return EntityAttachedPacket.set(packet, entity);
    }

    @Nullable
    public static Entity getEntityContext(Packet<?> packet) {
        return EntityAttachedPacket.get(packet);
    }

    public static void sendEntityType(ServerPlayer player, int entityId, EntityType<?> entityType) {
        PolymerServerProtocol.sendEntityInfo(player.connection, entityId, entityType);
    }

    public static void recreatePolymerEntity(Entity entity) {
        ((PolymerEntityProvider) entity).polymer$recreatePolymerEntity();
    }

    @ApiStatus.Experimental
    public static void setPolymerEntity(Entity entity, PolymerEntity polymerEntity) {
        ((PolymerEntityProvider) entity).polymer$setPolymerEntity(polymerEntity);
    }

    public static void refreshEntity(ServerPlayer player, Entity entity) {
        if (entity.level() instanceof ServerLevel world) {
            var tracker = ((ServerMapAccessor) world.getChunkSource().chunkMap).polymer$getEntityTrackers().get(entity.getId());
            if (tracker != null) {
                tracker.removePlayer(player);
                tracker.updatePlayer(player);
            }
        }
    }

    public static void refreshEntity(Entity entity) {
        if (entity.level() instanceof ServerLevel world) {
            var tracker = ((ServerMapAccessor) world.getChunkSource().chunkMap).polymer$getEntityTrackers().get(entity.getId());
            if (tracker != null) {
                for (var player : ((TrackedEntityAccessor) tracker).getSeenBy()) {
                    ((TrackedEntityAccessor) tracker).getServerEntity().removePairing(player.getPlayer());
                    ((TrackedEntityAccessor) tracker).getServerEntity().addPairing(player.getPlayer());
                }
            }
        }
    }

    public static boolean isPolymerEntityInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, Entity entity, InteractionResult actionResult) {
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null && polymerEntity.isPolymerEntityInteraction(player, hand, stack, world, actionResult)) {
            return true;
        } else if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isPolymerEntityInteraction(player, hand, stack, world, entity, actionResult)) {
            return true;
        }

        return POLYMER_ENTITY_INTERACTION_CHECK.invoker().isPolymerEntityInteraction(player, hand, stack, world, entity, actionResult);
    }


    public static <T extends Entity> void registerOverlay(EntityType<T> type, it.unimi.dsi.fastutil.Function<T, PolymerEntity> constructor) {
        registerOverlay(type, (Function<T, PolymerEntity>) constructor);
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, PolymerSyncedObject<EntityType<?>> syncedObject, it.unimi.dsi.fastutil.Function<T, PolymerEntity> constructor) {
        registerOverlay(type, syncedObject, (Function<T, PolymerEntity>) constructor);
    }


    @FunctionalInterface
    public interface PolymerEntityInteractionListener {
        boolean isPolymerEntityInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, Entity entity, InteractionResult actionResult);
    }
}

