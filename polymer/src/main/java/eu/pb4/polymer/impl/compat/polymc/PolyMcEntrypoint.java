package eu.pb4.polymer.impl.compat.polymc;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import io.github.theepicblock.polymc.api.PolyRegistry;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolyMcEntrypoint implements io.github.theepicblock.polymc.api.PolyMcEntrypoint {
    @Override
    public void registerPolys(PolyRegistry registry) {
        for (var entityType : Registry.ENTITY_TYPE) {
            if (PolymerEntityUtils.isRegisteredEntityType(entityType)) {
                registry.registerEntityPoly(entityType, (info, entity) -> null);
            }
        }
    }
}
