package eu.pb4.polymer.core.mixin.client.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.ClientUtils;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.other.PolymerTooltipContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(value = ItemGroup.class, priority = 2000)
public abstract class ItemGroupMixin implements ClientItemGroupExtension {
    @Shadow private ItemStack icon;

    @Shadow private Collection<ItemStack> displayStacks;
    @Shadow private Set<ItemStack> searchTabStacks;

    @Shadow @Final private ItemGroup.Type type;
    @Unique private List<ItemStack> polymer$itemsGroup = new ArrayList<>();
    @Unique private List<ItemStack> polymer$itemsSearch = new ArrayList<>();


    @Inject(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;reloadSearchProvider()V"))
    private void polymer$injectEntries(FeatureSet enabledFeatures, boolean operatorEnabled, CallbackInfo ci) {
        if (this.type == ItemGroup.Type.CATEGORY && ClientUtils.isClientThread()) {
            this.displayStacks.removeIf(PolymerImplUtils::isPolymerControlled);
            this.searchTabStacks.removeIf(PolymerImplUtils::isPolymerControlled);

            this.displayStacks.addAll(this.polymer$itemsGroup);
            this.searchTabStacks.addAll(this.polymer$itemsSearch);
        }
    }

    @Inject(method = "getIcon", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;", shift = At.Shift.AFTER))
    private void polymer$wrapIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (this.icon != null && this.icon.getItem() instanceof PolymerItem virtualItem) {
            this.icon = virtualItem.getPolymerItemStack(this.icon, PolymerTooltipContext.BASIC, ClientUtils.getPlayer());
        }
    }

    @Override
    public void polymer$addStackGroup(ItemStack stack) {
        this.polymer$itemsGroup.add(stack);
    }

    @Override
    public void polymer$addStackSearch(ItemStack stack) {
        this.polymer$itemsSearch.add(stack);
    }

    @Override
    public void polymer$clearStacks() {
        this.polymer$itemsGroup.clear();
        this.polymer$itemsSearch.clear();
    }

    @Override
    public Collection<ItemStack> polymer$getStacksGroup() {
        return this.polymer$itemsGroup;
    }

    public Collection<ItemStack> polymer$getStacksSearch() {
        return this.polymer$itemsSearch;
    }
}
