package eu.pb4.polymer.api.resourcepack;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public record PolymerModelData(Item item, int value, Identifier modelPath) {}
