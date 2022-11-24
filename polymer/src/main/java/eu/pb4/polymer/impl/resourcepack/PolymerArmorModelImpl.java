package eu.pb4.polymer.impl.resourcepack;

import eu.pb4.polymer.api.resourcepack.PolymerArmorModel;
import net.minecraft.util.Identifier;

public record PolymerArmorModelImpl(int color, Identifier modelPath) implements PolymerArmorModel {
}
