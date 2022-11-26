package eu.pb4.polymer.resourcepack.api;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents information about CustomModelData of item
 *
 * Values returned by methods might change in future!
 */
@ApiStatus.NonExtendable
public interface PolymerModelData {
    Item item();

    int value();

    Identifier modelPath();
}
