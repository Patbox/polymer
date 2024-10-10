package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

public class ItemDisplayElement extends DisplayElement {
    public ItemDisplayElement(ItemStack stack) {
        this.setItem(stack);
    }

    public ItemDisplayElement() {}

    public ItemDisplayElement(Item item) {
        this.setItem(item.getDefaultStack());
    }

    public void setItem(ItemStack stack) {
        this.dataTracker.set(DisplayTrackedData.Item.ITEM, stack);
    }

    public ItemStack getItem() {
        return this.dataTracker.get(DisplayTrackedData.Item.ITEM);
    }

    public void setModelTransformation(ModelTransformationMode mode) {
        this.dataTracker.set(DisplayTrackedData.Item.ITEM_DISPLAY, mode.getIndex());
    }

    public ModelTransformationMode getModelTransformation() {
        return ModelTransformationMode.FROM_INDEX.apply(this.dataTracker.get(DisplayTrackedData.Item.ITEM_DISPLAY));
    }


    @Override
    protected final EntityType<? extends DisplayEntity> getEntityType() {
        return EntityType.ITEM_DISPLAY;
    }
}
