package eu.pb4.polymer.api.client;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.utils.events.BooleanEvent;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientEntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * General utilities while dealing with client side integrations
 */
public final class PolymerClientUtils {
    public static final SimpleEvent<Runnable> ON_CLEAR = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> ON_SYNC_STARTED = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> ON_SYNC_FINISHED = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> ON_SEARCH_REBUILD = new SimpleEvent<>();

    @Nullable
    public static ClientPolymerBlock.State getPolymerStateAt(BlockPos pos) {
        return InternalClientRegistry.getBlockAt(pos);
    }

    @Nullable
    public static ClientPolymerEntityType getEntityType(Entity entity) {
        return InternalClientRegistry.ENTITY_TYPE.get(((ClientEntityExtension) entity).polymer_getId());
    }

    public static String getServerVersion() {
        return InternalClientRegistry.SERVER_VERSION;
    }

    public static boolean isEnabled() {
        return InternalClientRegistry.ENABLED;
    }
}
