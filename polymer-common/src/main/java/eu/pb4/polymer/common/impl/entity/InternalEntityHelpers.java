package eu.pb4.polymer.common.impl.entity;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.common.impl.FakeWorld;
import eu.pb4.polymer.common.mixin.SyncedEntityDataAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
@SuppressWarnings({"unused", "unchecked"})
public class InternalEntityHelpers {
    private static final Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();
    private static final Map<EntityType<?>, SynchedEntityData.DataItem<?>[]> TRACKED_DATA = new Object2ObjectOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);

    private static Player createPlayer() {
        Player player = null;
        try {
            player = new Player(FakeWorld.INSTANCE_UNSAFE, new GameProfile(Util.NIL_UUID, "TinyPotato")) {
                @Nullable
                @Override
                public GameType gameMode() {
                    return null;
                }

                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };
        } catch (Throwable e) {
            if (CommonImpl.LOG_MORE_ERRORS) {
                CommonImpl.LOGGER.error("Failed add player like entity! Trying with alternative method", e);
            }
            try {
                player = new Player(FakeWorld.INSTANCE_REGULAR, new GameProfile(Util.NIL_UUID, "TinyPotato")) {
                    @Nullable
                    @Override
                    public GameType gameMode() {
                        return null;
                    }

                    @Override
                    public boolean isSpectator() {
                        return false;
                    }

                    @Override
                    public boolean isCreative() {
                        return false;
                    }
                };
            } catch (Throwable e2) {
                if (CommonImpl.LOG_MORE_ERRORS) {
                    CommonImpl.LOGGER.error("Failed add player like entity!", e2);
                }
            }
        }
        EXAMPLE_ENTITIES.put(EntityType.PLAYER, player);
        return player;
    };

    public static SynchedEntityData.DataItem<?>[] getExampleTrackedDataOfEntityType(EntityType<?> type) {
        var val = TRACKED_DATA.get(type);

        if (val == null) {
            var ent = getEntity(type);
            if (ent != null) {
                var map = ((SyncedEntityDataAccessor) ent.getEntityData()).getItemsById();
                TRACKED_DATA.put(type, map);
                return map;
            }
        }

        return val;
    }

    public static <T extends Entity> Class<T> getEntityClass(EntityType<T> type) {
        return (Class<T>) getEntity(type).getClass();
    }

    public static boolean isLivingEntity(EntityType<?> type) {
        return getEntity(type) instanceof LivingEntity;
    }

    public static boolean isMobEntity(EntityType<?> type) {
        return getEntity(type) instanceof Mob;
    }

    public static boolean canPatchTrackedData(ServerPlayer player, Entity entity) {
        if (CompatStatus.DISGUISELIB) {
            return !((EntityDisguise) entity).isDisguised() || ((EntityDisguise) player).hasTrueSight();
        }

        return true;
    }

    public static Entity getEntity(EntityType<?> type) {
        Entity entity = EXAMPLE_ENTITIES.get(type);

        if (entity == null) {
            if (type == EntityType.PLAYER) {
                return createPlayer();
            }

            try {
                entity = type.create(FakeWorld.INSTANCE_UNSAFE, EntitySpawnReason.LOAD);
            } catch (Throwable e) {
                try {
                    entity = type.create(FakeWorld.INSTANCE_REGULAR, EntitySpawnReason.LOAD);
                } catch (Throwable e2) {
                    var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    if (CommonImpl.ENABLE_TEMPLATE_ENTITY_WARNINGS) {
                        CommonImpl.LOGGER.warn(String.format(
                                "Couldn't create template entity of %s... Defaulting to empty. %s",
                                id,
                                id.getNamespace().equals("minecraft") ? "This might cause problems!" : "Don't worry, this shouldn't cause problems!"
                        ));

                        if (id.getNamespace().equals("minecraft")) {
                            CommonImpl.LOGGER.warn("First error:");
                            e.printStackTrace();
                            CommonImpl.LOGGER.warn("Second error:");
                            e2.printStackTrace();
                        }
                    }
                    entity = FakeEntity.INSTANCE;
                }

            }
            EXAMPLE_ENTITIES.put(type, entity);
        }

        return entity;
    }

    public static Entity getFakeEntity() {
        return FakeEntity.INSTANCE;
    }
}
