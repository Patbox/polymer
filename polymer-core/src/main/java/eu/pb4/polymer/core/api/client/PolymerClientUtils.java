package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientEntityExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * General utilities while dealing with client side integrations
 */
@Environment(EnvType.CLIENT)
public final class PolymerClientUtils {
    private PolymerClientUtils() {
    }

    /**
     * This event is run after receiving server handshake packet
     */
    public static final SimpleEvent<Runnable> ON_HANDSHAKE = new SimpleEvent<>();
    /**
     * This event is run after clearing registries
     */
    public static final SimpleEvent<Runnable> ON_CLEAR = new SimpleEvent<>();
    /**
     * This event ir run before Polymer registry sync
     */
    public static final SimpleEvent<Runnable> ON_SYNC_STARTED = new SimpleEvent<>();
    /**
     * This event ir run after Polymer registry sync
     */
    public static final SimpleEvent<Runnable> ON_SYNC_FINISHED = new SimpleEvent<>();
    /**
     * This event ir run on rebuild of creative search
     */
    public static final SimpleEvent<Runnable> ON_SEARCH_REBUILD = new SimpleEvent<>();
    /**
     * This event is before client asks for sync request
     */
    public static final SimpleEvent<Runnable> ON_SYNC_REQUEST = new SimpleEvent<>();
    /**
     * This event is run after receiving an Polymer block update
     */
    public static final SimpleEvent<BiConsumer<BlockPos, ClientPolymerBlock.State>> ON_BLOCK_UPDATE = new SimpleEvent<>();
    /**
     * This event is run when Polymer functionality is disabled (good for clearing)
    */
    public static final SimpleEvent<Runnable> ON_DISABLE = new SimpleEvent<>();


    public static ClientPolymerBlock.State getPolymerStateAt(BlockPos pos) {
        return InternalClientRegistry.getBlockAt(pos);
    }

    public static void setPolymerStateAt(BlockPos pos, ClientPolymerBlock.State state) {
        InternalClientRegistry.setBlockAt(pos, state);
    }

    @Nullable
    public static ClientPolymerEntityType getEntityType(Entity entity) {
        return InternalClientRegistry.ENTITY_TYPES.get(((ClientEntityExtension) entity).polymer$getId());
    }

    public static String getServerVersion() {
        return InternalClientRegistry.serverVersion;
    }

    public static boolean isEnabled() {
        return InternalClientRegistry.enabled;
    }
}
