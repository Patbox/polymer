package eu.pb4.polymer.resourcepack.impl.generation;

import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public record PolymerModelDataImpl(Item item, int value, Identifier modelPath) implements PolymerModelData {
}
