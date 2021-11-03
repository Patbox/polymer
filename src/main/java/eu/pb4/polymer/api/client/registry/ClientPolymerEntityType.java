package eu.pb4.polymer.api.client.registry;

import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ClientPolymerEntityType (Identifier identifier, Text name) {
    public static final PolymerRegistry<ClientPolymerEntityType> REGISTRY = InternalClientRegistry.ENTITY_TYPE;
}
