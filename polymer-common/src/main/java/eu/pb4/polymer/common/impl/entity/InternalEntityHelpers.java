package eu.pb4.polymer.common.impl.entity;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.common.impl.FakeWorld;
import eu.pb4.polymer.common.mixin.DataTrackerAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
@SuppressWarnings({"unused", "unchecked"})
public class InternalEntityHelpers {
    private static final Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();
    private static final Map<EntityType<?>, DataTracker.Entry<?>[]> TRACKED_DATA = new Object2ObjectOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);

    private static PlayerEntity createPlayer() {
        PlayerEntity player = null;
        try {
            player = new PlayerEntity(FakeWorld.INSTANCE_UNSAFE, BlockPos.ORIGIN, 0, new GameProfile(Util.NIL_UUID, "TinyPotato")) {
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
                player = new PlayerEntity(FakeWorld.INSTANCE_REGULAR, BlockPos.ORIGIN, 0, new GameProfile(Util.NIL_UUID, "TinyPotato")) {
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

    public static DataTracker.Entry<?>[] getExampleTrackedDataOfEntityType(EntityType<?> type) {
        var val = TRACKED_DATA.get(type);

        if (val == null) {
            var ent = getEntity(type);
            if (ent != null) {
                var map = ((DataTrackerAccessor) ent.getDataTracker()).getEntries();
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
        return getEntity(type) instanceof MobEntity;
    }

    public static boolean canPatchTrackedData(ServerPlayerEntity player, Entity entity) {
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
                entity = type.create(FakeWorld.INSTANCE_UNSAFE);
            } catch (Throwable e) {
                try {
                    entity = type.create(FakeWorld.INSTANCE_REGULAR);
                } catch (Throwable e2) {
                    var id = Registries.ENTITY_TYPE.getId(type);
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
