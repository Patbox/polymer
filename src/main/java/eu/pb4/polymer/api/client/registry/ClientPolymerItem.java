package eu.pb4.polymer.api.client.registry;

import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record ClientPolymerItem(
        Identifier identifier,
        ItemStack visualStack,
        String itemGroup,
        int foodValue,
        float saturation,
        Identifier miningTool,
        int miningLevel
) {
    public static final PolymerRegistry<ClientPolymerItem> REGISTRY = InternalClientRegistry.ITEMS;
}
