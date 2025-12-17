package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
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
        this.dataTracker.set(DisplayTrackedData.Item.ITEM, stack);
    }

    public ItemStack getItem() {
        return this.dataTracker.get(DisplayTrackedData.Item.ITEM);
    }

    public void setItemDisplayContext(ItemDisplayContext mode) {
        this.dataTracker.set(DisplayTrackedData.Item.ITEM_DISPLAY, mode.getId());
    }
    public ItemDisplayContext getItemDisplayContext() {
        //noinspection DataFlowIssue
        return ItemDisplayContext.BY_ID.apply(this.dataTracker.get(DisplayTrackedData.Item.ITEM_DISPLAY));
    }

    @Deprecated(forRemoval = true)
    public void setModelTransformation(ItemDisplayContext mode) {
        setItemDisplayContext(mode);
    }
    @Deprecated(forRemoval = true)
    public ItemDisplayContext getModelTransformation() {
        return getItemDisplayContext();
    }

    @Override
    protected final EntityType<? extends Display> getEntityType() {
        return EntityType.ITEM_DISPLAY;
    }
}
