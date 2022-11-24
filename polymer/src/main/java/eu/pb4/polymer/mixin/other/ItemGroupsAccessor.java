package eu.pb4.polymer.mixin.other;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ItemGroups.class)
public interface ItemGroupsAccessor {
    //@Mutable
    //@Accessor
    //static void setGROUPS(ItemGroup[] GROUPS) {
    //    throw new UnsupportedOperationException();
    //}

    @Accessor
    static ItemGroup getSEARCH() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static List<ItemGroup> getGROUPS() {
        throw new UnsupportedOperationException();
    }

    @Mutable
    @Accessor
    static void setGROUPS(List<ItemGroup> GROUPS) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static List<ItemGroup> callCollect(ItemGroup... groups) {
        throw new UnsupportedOperationException();
    }

    @Invoker
    static void callUpdateEntries(FeatureSet enabledFeatures, boolean operatorEnabled) {
        throw new UnsupportedOperationException();
    }
}
