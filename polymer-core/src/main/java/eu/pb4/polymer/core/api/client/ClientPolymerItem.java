package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record ClientPolymerItem(
        Identifier identifier,
        ItemStack visualStack,
        @Nullable Item registryEntry
) implements ClientPolymerEntry<Item> {
    public static final PolymerRegistry<ClientPolymerItem> REGISTRY = InternalClientRegistry.ITEMS;
}
