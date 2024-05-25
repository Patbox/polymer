package eu.pb4.polymer.resourcepack.impl.generation;

import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

import java.util.List;

public record PolymerArmorModelImpl(int color, Identifier modelId, List<ArmorMaterial.Layer> layers) implements PolymerArmorModel {
    public static final PolymerArmorModel EMPTY = new PolymerArmorModelImpl(-1, Identifier.of("empty"), List.of());
}
