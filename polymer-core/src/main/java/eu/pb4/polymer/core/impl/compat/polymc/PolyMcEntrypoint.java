package eu.pb4.polymer.core.impl.compat.polymc;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.theepicblock.polymc.api.PolyRegistry;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolyMcEntrypoint implements io.github.theepicblock.polymc.api.PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        for (var entityType : Registries.ENTITY_TYPE) {
            if (PolymerEntityUtils.isPolymerEntityType(entityType)) {
                registry.registerEntityPoly(entityType, (info, entity) -> null);
            }
        }

        for (var item : Registries.ITEM) {
            if (item instanceof PolymerItem) {
                registry.registerItemPoly(item, (x, y, z) -> x.copy());
            }
        }
    }
}
