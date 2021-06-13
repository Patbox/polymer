package eu.pb4.polymer.resourcepack;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public record CMDInfo(Item item, int value, Identifier modelPath) {}
