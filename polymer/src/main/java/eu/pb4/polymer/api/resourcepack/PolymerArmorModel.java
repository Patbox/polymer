package eu.pb4.polymer.api.resourcepack;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents information about armor texture
 *
 * Values returned by methods might change in future!
 */
@ApiStatus.NonExtendable
public interface PolymerArmorModel{
    int color();
    Identifier modelPath();
}
