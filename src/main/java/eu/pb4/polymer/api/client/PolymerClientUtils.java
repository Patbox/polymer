package eu.pb4.polymer.api.client;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.util.math.BlockPos;

/**
 * General utilities while dealing with client side integrations
 */
public final class PolymerClientUtils {
    public static ClientPolymerBlock.State getPolymerStateAt(BlockPos pos) {
        return InternalClientRegistry.getBlockAt(pos);
    }
}
