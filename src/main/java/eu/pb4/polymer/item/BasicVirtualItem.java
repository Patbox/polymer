package eu.pb4.polymer.item;

import net.minecraft.item.Item;

/**
 * Basic implementation of VirtualItem
 */
public class BasicVirtualItem extends Item implements VirtualItem {
    private final Item virtualItem;

    public BasicVirtualItem(Settings settings, Item virtualItem) {
        super(settings);
        this.virtualItem = virtualItem;
    }

    @Override
    public Item getVirtualItem() {
        return virtualItem;
    }
}
