package eu.pb4.polymer.impl.compat.polymc;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.item.PolymerItem;
import io.github.theepicblock.polymc.api.PolyRegistry;
import net.minecraft.util.registry.Registries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolyMcEntrypoint implements io.github.theepicblock.polymc.api.PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        for (var entityType : Registries.ENTITY_TYPE) {
            if (PolymerEntityUtils.isRegisteredEntityType(entityType)) {
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
