package eu.pb4.polymer.api.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Basic implementation of PolymerItem for blocks
 */
public class PolymerBlockItem extends BlockItem implements PolymerItem {
    private final Item virtualItem;

    public PolymerBlockItem(Block block, Settings settings, Item virtualItem) {
        super(block, settings);
        this.virtualItem = virtualItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return virtualItem;
    }
}
