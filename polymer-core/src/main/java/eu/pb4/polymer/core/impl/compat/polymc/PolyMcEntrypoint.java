package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import io.github.theepicblock.polymc.api.PolyRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolyMcEntrypoint implements io.github.theepicblock.polymc.api.PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        for (var entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (PolymerEntityUtils.isPolymerEntityType(entityType)) {
                registry.registerEntityPoly(entityType, PassthroughPoly.entity());
            }
        }

        for (var item : BuiltInRegistries.ITEM) {
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, item) != null) {
                registry.registerItemPoly(item, PassthroughPoly.ITEM);
            }
        }
    }
}
