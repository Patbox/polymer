package eu.pb4.polymer.core.api.utils;

import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Used to mark client-synchronized polymer objects like BlockEntities, Enchantments, Recipes, etc
 */

public interface PolymerSyncedObject<T> extends PolymerObject {

    /**
     * Generic method to get polymer replacement sent to player
     *
     * @param context target context
     * @return a replacement. It shouldn't be a null unless specified otherwise
     */
    T getPolymerReplacement(PacketContext context);

    /**
     * Allows to gate syncing of this object with clients running polymer
     */
    default boolean canSynchronizeToPolymerClient(PacketContext context) {
        return true;
    }

    /**
     * Allows to mark it to still send it to supported clients (for client optional setups)
     * Currently used for tags
     */
    default boolean canSyncRawToClient(PacketContext context) {
        return false;
    }

    static <T> boolean canSyncRawToClient(Registry<T> registry, T obj, PacketContext context) {
        return obj instanceof PolymerSyncedObject<?> pol ? pol.canSyncRawToClient(context) : !PolymerUtils.isServerOnly(registry, obj);
    }
}
