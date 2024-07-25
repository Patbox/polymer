package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import eu.pb4.polymer.core.impl.other.ItemGroupEntriesImpl;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
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

@Mixin(value = ItemGroup.class, priority = 800)
public abstract class ItemGroupMixin implements ItemGroupExtra {
    @Shadow @Final private ItemGroup.EntryCollector entryCollector;

    @Shadow private Collection<ItemStack> displayStacks;

    @Shadow private Set<ItemStack> searchTabStacks;

    @Override
    public PolymerItemGroupUtils.Contents polymer$getContentsWith(Identifier id, FeatureSet enabledFeatures, boolean operatorEnabled, RegistryWrapper.WrapperLookup lookup) {
        var collector = new ItemGroupEntriesImpl((ItemGroup) (Object) this, enabledFeatures);
        var context = new ItemGroup.DisplayContext(enabledFeatures, operatorEnabled, lookup);
        this.entryCollector.accept(context, collector);
        var parent = new LinkedList<>(collector.parentTabStacks);
        var search = new LinkedList<>(collector.searchTabStacks);
        PolymerImplUtils.callItemGroupEvents(id, (ItemGroup) (Object) this, parent, search, context);
        return new PolymerItemGroupUtils.Contents(parent, search);
    }

    @ModifyArg(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getKey(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Object polymerCore$bypassServerSide(Object entry) {
        return PolymerItemGroupUtils.isPolymerItemGroup((ItemGroup) entry) ? ItemGroups.getDefaultTab() : entry;
    }

    @Inject(method = "updateEntries", at = @At(value = "TAIL"), cancellable = true)
    private void polymerCore$bypassFabricApiBS(ItemGroup.DisplayContext displayContext, CallbackInfo ci) {
        if (PolymerItemGroupUtils.isPolymerItemGroup((ItemGroup) (Object) this) || this instanceof PolymerObject) {
            var parent = new LinkedList<>(this.displayStacks);
            var search = new LinkedList<>(this.searchTabStacks);
            PolymerImplUtils.callItemGroupEvents(PolymerItemGroupUtils.getId((ItemGroup) (Object) this), (ItemGroup) (Object) this, parent, search, displayContext);
            this.displayStacks.clear();
            this.displayStacks.addAll(parent);
            this.searchTabStacks.clear();
            this.searchTabStacks.addAll(search);
            ci.cancel();
        }
    }
}
