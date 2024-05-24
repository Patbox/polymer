package eu.pb4.polymer.resourcepack.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents information about armor texture
 */
@ApiStatus.NonExtendable
public interface PolymerArmorModel{
    int color();
    Identifier modelPath();
}
