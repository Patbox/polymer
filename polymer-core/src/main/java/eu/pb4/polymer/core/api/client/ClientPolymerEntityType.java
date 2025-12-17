package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ClientPolymerEntityType(Identifier identifier, Component name, @Nullable EntityType<?> registryEntry) implements ClientPolymerEntry<EntityType<?>> {

    public ClientPolymerEntityType (Identifier identifier, Component name) {
        this(identifier, name, null);
    }
    public static final PolymerRegistry<ClientPolymerEntityType> REGISTRY = InternalClientRegistry.ENTITY_TYPES;
}
