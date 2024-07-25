package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

public interface ItemGroupExtra {
    PolymerItemGroupUtils.Contents polymer$getContentsWith(Identifier id, FeatureSet enabledFeatures, boolean operatorEnabled, RegistryWrapper.WrapperLookup lookup);
    default boolean polymer$isSyncable() {
        return true;
    }
}
