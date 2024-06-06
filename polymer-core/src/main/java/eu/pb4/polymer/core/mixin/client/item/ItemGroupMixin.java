package eu.pb4.polymer.core.mixin.client.item;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
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
    @Shadow private Collection<ItemStack> displayStacks;
    @Shadow private Set<ItemStack> searchTabStacks;

    @Shadow @Final private ItemGroup.Type type;
    @Mutable
    @Shadow @Final private ItemGroup.Row row;
    @Mutable
    @Shadow @Final private int column;


    @Unique private final List<ItemStack> polymer$itemsGroup = new ArrayList<>();
    @Unique private final List<ItemStack> polymer$itemsSearch = new ArrayList<>();
    @Unique
    private int polymerCore$page;

    @ModifyArg(method = "updateEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getKey(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Object polymerCore$bypassServerSide(Object entry) {
        return entry instanceof InternalClientItemGroup ? ItemGroups.getDefaultTab() : entry;
    }

    @Inject(method = "updateEntries", at = @At("HEAD"), cancellable = true)
    private void polymer$injectEntriesCustom(ItemGroup.DisplayContext arg, CallbackInfo ci) {
        if (((Object) this) instanceof InternalClientItemGroup) {
            this.displayStacks.clear();
            this.searchTabStacks.clear();
            this.displayStacks.addAll(this.polymer$itemsGroup);
            this.searchTabStacks.addAll(this.polymer$itemsSearch);
            ci.cancel();
        }
    }

    @Inject(method = "updateEntries", at = @At("TAIL"))
    private void polymer$injectEntriesVanilla(ItemGroup.DisplayContext arg, CallbackInfo ci) {
        if (this.type == ItemGroup.Type.CATEGORY && ClientUtils.isClientThread()) {
            this.displayStacks.removeIf(PolymerImplUtils::removeFromItemGroup);
            this.searchTabStacks.removeIf(PolymerImplUtils::removeFromItemGroup);

            this.displayStacks.addAll(this.polymer$itemsGroup);
            this.searchTabStacks.addAll(this.polymer$itemsSearch);
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
