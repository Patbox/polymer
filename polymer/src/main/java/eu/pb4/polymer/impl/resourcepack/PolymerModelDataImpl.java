package eu.pb4.polymer.impl.resourcepack;

import eu.pb4.polymer.api.resourcepack.PolymerModelData;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public record PolymerModelDataImpl(Item item, int value, Identifier modelPath) implements PolymerModelData {
}
