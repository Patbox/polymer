package eu.pb4.polymer.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public class VirtualBlockItem extends BlockItem implements VirtualItem {
    private final Item virtualItem;

    public VirtualBlockItem(Block block, Settings settings, Item virtualItem) {
        super(block, settings);
        this.virtualItem = virtualItem;
    }

    @Override
    public Item getVirtualItem() {
        return virtualItem;
    }
}
