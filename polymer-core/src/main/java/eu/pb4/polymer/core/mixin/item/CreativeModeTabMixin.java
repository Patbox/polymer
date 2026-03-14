package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.CreativeModeTabExtra;
import eu.pb4.polymer.core.impl.other.ItemGroupEntriesImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

@Mixin(value = CreativeModeTab.class, priority = 800)
public abstract class CreativeModeTabMixin implements CreativeModeTabExtra {
    @Shadow @Final private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;

    @Shadow private Collection<ItemStack> displayItems;

    @Shadow private Set<ItemStack> displayItemsSearchTab;

    @Override
    public PolymerItemGroupUtils.Contents polymer$getContentsWith(Identifier id, FeatureFlagSet enabledFeatures, boolean operatorEnabled, HolderLookup.Provider lookup) {
        var collector = new ItemGroupEntriesImpl((CreativeModeTab) (Object) this, enabledFeatures);
        var context = new CreativeModeTab.ItemDisplayParameters(enabledFeatures, operatorEnabled, lookup);
        this.displayItemsGenerator.accept(context, collector);
        var parent = new LinkedList<>(collector.parentTabStacks);
        var search = new LinkedList<>(collector.searchTabStacks);
        PolymerImplUtils.callItemGroupEvents(id, (CreativeModeTab) (Object) this, parent, search, context);
        parent.removeIf(ItemStack::isEmpty);
        search.removeIf(ItemStack::isEmpty);
        return new PolymerItemGroupUtils.Contents(parent, search);
    }

    /*@ModifyArg(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getResourceKey(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Object polymerCore$bypassServerSide(Object entry) {
        return PolymerItemGroupUtils.isPolymerItemGroup((CreativeModeTab) entry) ? CreativeModeTabs.getDefaultTab() : entry;
    }*/

    @Inject(method = "buildContents", at = @At(value = "TAIL"), cancellable = true)
    private void polymerCore$bypassFabricApiBS(CreativeModeTab.ItemDisplayParameters displayContext, CallbackInfo ci) {
        if (PolymerItemGroupUtils.isPolymerItemGroup((CreativeModeTab) (Object) this) || this instanceof PolymerObject) {
            var parent = new LinkedList<>(this.displayItems);
            var search = new LinkedList<>(this.displayItemsSearchTab);
            PolymerImplUtils.callItemGroupEvents(PolymerItemGroupUtils.getId((CreativeModeTab) (Object) this), (CreativeModeTab) (Object) this, parent, search, displayContext);
            this.displayItems.clear();
            this.displayItems.addAll(parent);
            this.displayItemsSearchTab.clear();
            this.displayItemsSearchTab.addAll(search);
            ci.cancel();
        }
    }
}
