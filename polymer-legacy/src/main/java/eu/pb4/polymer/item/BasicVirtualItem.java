package eu.pb4.polymer.item;

import net.minecraft.item.Item;

/**
 * Use {@link eu.pb4.polymer.api.item.SimplePolymerItem} instead
 */
@Deprecated
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
