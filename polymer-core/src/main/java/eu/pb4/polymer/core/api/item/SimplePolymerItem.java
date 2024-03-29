package eu.pb4.polymer.core.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation of PolymerItem
 */
public class SimplePolymerItem extends Item implements PolymerItem {
    private final Item polymerItem;

    public SimplePolymerItem(Settings settings, Item polymerItem) {
        super(settings);
        this.polymerItem = polymerItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerItem;
    }
}
