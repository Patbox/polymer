package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(value = ItemGroup.class, priority = 3000)
public abstract class ItemGroupMixin implements ItemGroupExtra {
    @Shadow private Collection<ItemStack> displayStacks;


    @Shadow private Set<ItemStack> searchTabStacks;

    @Shadow @Nullable private Consumer<List<ItemStack>> searchProviderReloader;


    @Shadow public abstract void updateEntries(ItemGroup.DisplayContext arg);

    @Shadow public abstract void reloadSearchProvider();

    @Override
    public PolymerItemGroupUtils.Contents polymer$getContentsWith(FeatureSet enabledFeatures, boolean operatorEnabled, RegistryWrapper.WrapperLookup lookup) {
        var oldDisplayStack = this.displayStacks;
        var oldSearchStack = this.searchTabStacks;
        var oldSearchProvider = this.searchProviderReloader;

        this.searchProviderReloader = null;

        this.updateEntries(new ItemGroup.DisplayContext(enabledFeatures, operatorEnabled, lookup));
        var contents = new PolymerItemGroupUtils.Contents(this.displayStacks, this.searchTabStacks);

        this.displayStacks = oldDisplayStack;
        this.searchTabStacks = oldSearchStack;
        this.searchProviderReloader = oldSearchProvider;

        return contents;
    }

    @ModifyArg(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getKey(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Object polymerCore$bypassServerSide(Object entry) {
        return PolymerItemGroupUtils.isPolymerItemGroup((ItemGroup) entry) ? ItemGroups.getDefaultTab() : entry;
    }

    @Inject(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;reloadSearchProvider()V", shift = At.Shift.BEFORE), cancellable = true)
    private void polymerCore$bypassFabricApiBS(ItemGroup.DisplayContext displayContext, CallbackInfo ci) {
        if (PolymerItemGroupUtils.isPolymerItemGroup((ItemGroup) (Object) this) || this instanceof PolymerObject) {
            this.reloadSearchProvider();
            ci.cancel();
        }
    }
}
