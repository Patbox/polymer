package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

public interface ItemGroupExtra {
    PolymerItemGroupUtils.Contents polymer$getContentsWith(FeatureSet enabledFeatures, boolean operatorEnabled);

    default Identifier polymer$getId() {
        return PolymerImplUtils.toItemGroupId((ItemGroup) this);
    }

    default boolean polymer$isSyncable() {
        return true;
    }
}
