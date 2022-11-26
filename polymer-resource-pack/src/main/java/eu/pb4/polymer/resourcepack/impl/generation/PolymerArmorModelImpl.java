package eu.pb4.polymer.resourcepack.impl.generation;

import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import net.minecraft.util.Identifier;

public record PolymerArmorModelImpl(int color, Identifier modelPath) implements PolymerArmorModel {
}
