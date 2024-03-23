package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PolymerItemComponent {
    default boolean canSyncRawToClient(ServerPlayerEntity player) {
        return false;
    }
}
