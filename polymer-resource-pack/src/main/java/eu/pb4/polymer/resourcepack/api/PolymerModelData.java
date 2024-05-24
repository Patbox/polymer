package eu.pb4.polymer.resourcepack.api;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents information about CustomModelData of item
 */
@ApiStatus.NonExtendable
public interface PolymerModelData {
    Item item();
    int value();
    Identifier modelPath();
    default CustomModelDataComponent asComponent() {
        return new CustomModelDataComponent(value());
    }
    default ItemStack asStack() {
        var stack = item().getDefaultStack();
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, asComponent());
        return stack;
    }
}
