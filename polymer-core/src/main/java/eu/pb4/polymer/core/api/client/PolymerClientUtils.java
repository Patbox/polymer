package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.common.impl.EventImplUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import eu.pb4.polymer.core.impl.client.interfaces.ClientEntityExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    public static final Event<Runnable> ON_HANDSHAKE = EventImplUtils.createRunnableEvent();
    /**
     * This event is run after clearing registries
     */
    public static final Event<Runnable> ON_CLEAR = EventImplUtils.createRunnableEvent();
    /**
     * This event ir run before Polymer registry sync
     */
    public static final Event<Runnable> ON_SYNC_STARTED = EventImplUtils.createRunnableEvent();
    /**
     * This event ir run after Polymer registry sync
     */
    public static final Event<Runnable> ON_SYNC_FINISHED = EventImplUtils.createRunnableEvent();
    /**
     * This event ir run on rebuild of creative search
     */
    public static final Event<Runnable> ON_SEARCH_REBUILD = EventImplUtils.createRunnableEvent();
    /**
     * This event is before client asks for sync request
     */
    public static final Event<Runnable> ON_SYNC_REQUEST = EventImplUtils.createRunnableEvent();
    /**
     * This event is run after receiving an Polymer block update
     */
    public static final Event<BiConsumer<BlockPos, ClientPolymerBlock.State>> ON_BLOCK_UPDATE = EventImplUtils.createBiConsumerEvent();
    /**
     * This event is run when Polymer functionality is disabled (good for clearing)
    */
    public static final Event<Runnable> ON_DISABLE = EventImplUtils.createRunnableEvent();


    public static ClientPolymerBlock.State getPolymerStateAt(BlockPos pos) {
        return InternalClientRegistry.getBlockAt(pos);
    }

    public static void setPolymerStateAt(BlockPos pos, ClientPolymerBlock.State state) {
        InternalClientRegistry.setBlockAt(pos, state);
    }

    @Nullable
    public static ClientPolymerEntityType getEntityType(Entity entity) {
        return entity != null ? InternalClientRegistry.ENTITY_TYPES.get(((ClientEntityExtension) entity).polymer$getId()) : null;
    }

    public static String getServerVersion() {
        return InternalClientRegistry.serverVersion;
    }

    public static boolean isEnabled() {
        return InternalClientRegistry.enabled;
    }

    /**
     * Returns a key object for specific ItemStack representing uniquelly it's type.
     * Can be used as a alternative to using Identifier for type comparison
     * in recipe viewers and other cases.
     *
     * Return null for non-polymer stacks, Item for polymer-wrapped regular items
     * and custom object for polymer items.
     */
    @Nullable
    public static Object getUniqueKey(ItemStack stack) {
        return CompatUtils.getKey(stack);
    }

    /**
     * Check if two polymer item stacks are of the same type.
     */
    public static boolean areSameType(ItemStack a, ItemStack b) {
        return CompatUtils.areSamePolymerType(a, b);
    }

    public static boolean isPolymerControlledItemStack(ItemStack stack) {
        return PolymerImplUtils.isPolymerControlled(stack);
    }

    public static void provideCreativeModePolymerItems(Consumer<ItemStack> consumer) {
        CompatUtils.iterateItems(consumer);
    }
}
