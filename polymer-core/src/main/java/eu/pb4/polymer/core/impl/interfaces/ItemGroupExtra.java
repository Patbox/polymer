package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.resource.featuretoggle.FeatureSet;

public interface ItemGroupExtra {
    PolymerItemGroupUtils.Contents polymer$getContentsWith(FeatureSet enabledFeatures, boolean operatorEnabled);
    default boolean polymer$isSyncable() {
        return true;
    }
}
