package eu.pb4.polymer.api.client;

import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ClientPolymerEntityType (Identifier identifier, Text name, @Nullable EntityType<?> registryEntry) implements ClientPolymerEntry<EntityType<?>> {

    public ClientPolymerEntityType (Identifier identifier, Text name) {
        this(identifier, name, null);
    }
    public static final PolymerRegistry<ClientPolymerEntityType> REGISTRY = InternalClientRegistry.ENTITY_TYPES;
}
