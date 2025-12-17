package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CreativeModeTabExtra {
    PolymerItemGroupUtils.Contents polymer$getContentsWith(Identifier id, FeatureFlagSet enabledFeatures, boolean operatorEnabled, HolderLookup.Provider lookup);
    default boolean polymer$isSyncable() {
        return true;
    }
}
