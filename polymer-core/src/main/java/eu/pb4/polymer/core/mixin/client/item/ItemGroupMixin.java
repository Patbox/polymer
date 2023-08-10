package eu.pb4.polymer.core.mixin.client.item;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.other.PolymerTooltipContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
@Mixin(value = ItemGroup.class, priority = 1200)
public abstract class ItemGroupMixin implements ClientItemGroupExtension {
    @Shadow private ItemStack icon;

    @Shadow private Collection<ItemStack> displayStacks;
    @Shadow private Set<ItemStack> searchTabStacks;

    @Shadow @Final private ItemGroup.Type type;
    @Mutable
    @Shadow @Final private ItemGroup.Row row;
    @Mutable
    @Shadow @Final private int column;

    @Shadow public abstract void reloadSearchProvider();

    @Unique private final List<ItemStack> polymer$itemsGroup = new ArrayList<>();
    @Unique private final List<ItemStack> polymer$itemsSearch = new ArrayList<>();
    @Unique
    private int polymerCore$page;

    @ModifyArg(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getKey(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Object polymerCore$bypassServerSide(Object entry) {
        return entry instanceof InternalClientItemGroup ? ItemGroups.getDefaultTab() : entry;
    }

    @Inject(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;reloadSearchProvider()V", shift = At.Shift.BEFORE), cancellable = true)
    private void polymer$injectEntriesDynamic(ItemGroup.DisplayContext displayContext, CallbackInfo ci) {
        if (((Object) this) instanceof InternalClientItemGroup) {
            this.displayStacks.addAll(this.polymer$itemsGroup);
            this.searchTabStacks.addAll(this.polymer$itemsSearch);
            this.reloadSearchProvider();
            ci.cancel();
        }
    }

    @Inject(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;reloadSearchProvider()V"))
    private void polymer$injectEntriesVanilla(ItemGroup.DisplayContext arg, CallbackInfo ci) {
        if (this.type == ItemGroup.Type.CATEGORY && ClientUtils.isClientThread()) {
            this.displayStacks.removeIf(PolymerImplUtils::removeFromItemGroup);
            this.searchTabStacks.removeIf(PolymerImplUtils::removeFromItemGroup);

            this.displayStacks.addAll(this.polymer$itemsGroup);
            this.searchTabStacks.addAll(this.polymer$itemsSearch);
        }
    }

    @Inject(method = "getIcon", at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;", shift = At.Shift.AFTER))
    private void polymer$wrapIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (this.icon != null && this.icon.getItem() instanceof PolymerItem virtualItem && !PolymerClientDecoded.checkDecode(this.icon.getItem())) {
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

    @Override
    public void polymerCore$setPos(ItemGroup.Row row, int slot) {
        this.row = row;
        this.column = slot;
    }

    @Override
    public void polymerCore$setPage(int page) {
        this.polymerCore$page = page;
    }

    @Override
    public int polymerCore$getPage() {
        return this.polymerCore$page;
    }
}
