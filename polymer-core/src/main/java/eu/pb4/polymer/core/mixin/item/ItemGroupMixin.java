package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ItemGroupExtra {
    @Shadow private Collection<ItemStack> displayStacks;


    @Shadow private Set<ItemStack> searchTabStacks;

    @Shadow @Nullable private Consumer<List<ItemStack>> searchProviderReloader;

    @Shadow public abstract void updateEntries(FeatureSet enabledFeatures, boolean operatorEnabled);

    @Override
    public PolymerItemGroupUtils.Contents polymer$getContentsWith(FeatureSet enabledFeatures, boolean operatorEnabled) {
        var oldDisplayStack = this.displayStacks;
        var oldSearchStack = this.searchTabStacks;
        var oldSearchProvider = this.searchProviderReloader;

        this.searchProviderReloader = null;

        this.updateEntries(enabledFeatures, operatorEnabled);
        var contents = new PolymerItemGroupUtils.Contents(this.displayStacks, this.searchTabStacks);

        this.displayStacks = oldDisplayStack;
        this.searchTabStacks = oldSearchStack;
        this.searchProviderReloader = oldSearchProvider;

        return contents;
    }
}
