package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PolymerItemComponent extends PolymerObject {
    default boolean canSyncRawToClient(ServerPlayerEntity player) {
        return false;
    }
}
