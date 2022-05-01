package eu.pb4.polymer.impl.entity;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.other.FakeWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.disguiselib.api.EntityDisguise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@SuppressWarnings({"unused", "unchecked"})
public class InternalEntityHelpers {
    private static final Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();
    private static final PigEntity PIG = new PigEntity(EntityType.PIG, FakeWorld.INSTANCE);

    public static List<DataTracker.Entry<?>> getExampleTrackedDataOfEntityType(EntityType<?> type) {
        return getEntity(type).getDataTracker().getAllEntries();
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
            if (((EntityDisguise) entity).isDisguised() && !((EntityDisguise) player).hasTrueSight()) {
                return false;
            }
        }

        return true;
    }

    public static Entity getEntity(EntityType<?> type) {
        Entity entity = EXAMPLE_ENTITIES.get(type);

        if (entity == null) {
            try {
                entity = type.create(FakeWorld.INSTANCE);
            } catch (Throwable e) {
                var id = Registry.ENTITY_TYPE.getId(type);
                if (PolymerImpl.ENABLE_TEMPLATE_ENTITY_WARNINGS) {
                    PolymerImpl.LOGGER.warn(String.format(
                            "Couldn't create template entity of %s (%s)... Defaulting to empty. %s",
                            id,
                            type.getBaseClass().toString(),
                            id != null && id.getNamespace().equals("minecraft") ? "This might cause problems!" : "Don't worry, this shouldn't cause problems!"
                    ));

                    if (id != null && id.getNamespace().equals("minecraft")) {
                        e.printStackTrace();
                    }
                }
                entity = FakeEntity.INSTANCE;
            }
            EXAMPLE_ENTITIES.put(type, entity);
        }

        return entity;
    }

    public static Entity getFakeEntity() {
        return FakeEntity.INSTANCE;
    }

    public static PigEntity getTacticalPig() {
        return PIG;
    }

    static {
        EXAMPLE_ENTITIES.put(EntityType.PLAYER, new PlayerEntity(FakeWorld.INSTANCE, BlockPos.ORIGIN, 0, new GameProfile(Util.NIL_UUID, "TinyPotato")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        });

        EXAMPLE_ENTITIES.put(EntityType.PIG, PIG);
    }
}
