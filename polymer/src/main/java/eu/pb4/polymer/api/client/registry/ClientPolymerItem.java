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
        String itemGroup,
        int foodValue,
        float saturation,
        Identifier miningTool,
        int miningLevel,
        int stackSize,
        @Nullable Item registryEntry
) implements ClientPolymerEntry<Item> {

    public ClientPolymerItem(
            Identifier identifier,
            ItemStack visualStack,
            String itemGroup,
            int foodValue,
            float saturation,
            Identifier miningTool,
            int miningLevel,
            @Nullable Item registryEntry
    ) {
        this(identifier, visualStack, itemGroup, foodValue, saturation, miningTool, miningLevel, -1, registryEntry);
    }

    public ClientPolymerItem(
            Identifier identifier,
            ItemStack visualStack,
            String itemGroup,
            int foodValue,
            float saturation,
            Identifier miningTool,
            int miningLevel
    ) {
        this(identifier, visualStack, itemGroup, foodValue, saturation, miningTool, miningLevel, null);
    }

    @Deprecated
    @Nullable
    public Item realServerItem() {
        return this.registryEntry;
    }

    public static final PolymerRegistry<ClientPolymerItem> REGISTRY = InternalClientRegistry.ITEMS;
}
