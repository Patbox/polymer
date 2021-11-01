package eu.pb4.polymer.api.client;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.utils.events.BooleanEvent;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * General utilities while dealing with client side integrations
 */
public final class PolymerClientUtils {
    public static final SimpleEvent<Runnable> ON_CLEAR = new SimpleEvent<>();
    public static final SimpleEvent<Consumer<ClientPolymerItem>> ON_ITEM_SYNC = new SimpleEvent<>();
    public static final SimpleEvent<Runnable> ON_SEARCH_REBUILD = new SimpleEvent<>();

    public static ClientPolymerBlock.State getPolymerStateAt(BlockPos pos) {
        return InternalClientRegistry.getBlockAt(pos);
    }
}
