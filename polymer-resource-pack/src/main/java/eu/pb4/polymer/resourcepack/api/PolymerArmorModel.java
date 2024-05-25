package eu.pb4.polymer.resourcepack.api;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

/**
 * Represents information about armor texture
 */
@ApiStatus.NonExtendable
public interface PolymerArmorModel {
    int color();
    Identifier modelId();
    List<ArmorMaterial.Layer> layers();
    @Deprecated
    default Identifier modelPath() {
        return modelId();
    }
}
