package eu.pb4.polymer.api.client.registry;

import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public record ClientPolymerItem(
        Identifier identifier,
        ItemStack visualStack,
        int foodValue,
        float saturation,
        Identifier miningTool,
        int miningLevel,
        int stackSize,
        @Nullable Item registryEntry
) implements ClientPolymerEntry<Item> {
    @Deprecated
    @Nullable
    public Item realServerItem() {
        return this.registryEntry;
    }

    public static final PolymerRegistry<ClientPolymerItem> REGISTRY = InternalClientRegistry.ITEMS;
}
