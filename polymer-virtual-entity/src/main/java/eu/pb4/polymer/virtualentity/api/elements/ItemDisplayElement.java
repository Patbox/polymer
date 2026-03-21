package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.DisplayEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemDisplayElement extends DisplayElement {
    public ItemDisplayElement(ItemStack stack) {
        this.setItem(stack);
    }

    public ItemDisplayElement() {}

    public ItemDisplayElement(Item item) {
        this.setItem(item.getDefaultInstance());
    }

    public void setItem(ItemStack stack) {
        this.syncedData.set(DisplayEntityData.Item.ITEM, stack);
    }

    public ItemStack getItem() {
        return this.syncedData.get(DisplayEntityData.Item.ITEM);
    }

    public void setItemDisplayContext(ItemDisplayContext mode) {
        this.syncedData.set(DisplayEntityData.Item.ITEM_DISPLAY, mode.getId());
    }
    public ItemDisplayContext getItemDisplayContext() {
        //noinspection DataFlowIssue
        return ItemDisplayContext.BY_ID.apply(this.syncedData.get(DisplayEntityData.Item.ITEM_DISPLAY));
    }

    @Override
    protected final EntityType<? extends Display> getEntityType() {
        return EntityType.ITEM_DISPLAY;
    }
}
