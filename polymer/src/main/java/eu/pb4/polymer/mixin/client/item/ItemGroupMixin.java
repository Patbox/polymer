package eu.pb4.polymer.mixin.client.item;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ClientItemGroupExtension {
    @Shadow private ItemStack icon;

    @Shadow @Nullable private ItemStackSet searchTabStacks;

    @Shadow @Nullable private ItemStackSet displayStacks;

    @Unique private List<ItemStack> polymer_items = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_storeInMap(int index, Text displayName, CallbackInfo ci) {
        if (!(this instanceof PolymerObject)) {
            InternalClientRegistry.VANILLA_ITEM_GROUPS.put("vanilla_" + index, (ItemGroup) (Object) this);
        }
    }

    @Inject(method = "getStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;addItems(Lnet/minecraft/resource/featuretoggle/FeatureSet;Lnet/minecraft/item/ItemGroup$Entries;)V",
            shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer_addItems(FeatureSet enabledFeatures, boolean search, CallbackInfoReturnable<ItemStackSet> cir, ItemGroup.EntriesImpl entries) {
        entries.addAll(this.polymer_items);
    }

    @Inject(method = "getIcon", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemGroup;createIcon()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void polymer_wrapIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (this.icon.getItem() instanceof PolymerItem virtualItem) {
            this.icon = virtualItem.getPolymerItemStack(this.icon, null);
        }
    }

    /*@Inject(method = "appendStacks", at = @At("TAIL"))
    private void polymer_appendStacks(DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        try {
            if (ClientUtils.isClientThread() && stacks != null) {
                stacks.removeIf((s) -> PolymerItemUtils.isPolymerServerItem(s));
                if (((Object) this) == ItemGroup.SEARCH) {
                    for (var group : ItemGroup.GROUPS) {
                        if (group instanceof InternalClientItemGroup clientItemGroup && clientItemGroup.getStacks() != null) {
                            stacks.addAll(clientItemGroup.getStacks());
                        } else if (((ClientItemGroupExtension) group).polymer_getStacks() != null) {
                            stacks.addAll(((ClientItemGroupExtension) group).polymer_getStacks());
                        }
                    }
                } else if (this.polymer_items != null) {
                    stacks.addAll(this.polymer_items);
                }
            }
        } catch (Throwable e) {

        }
    }*/

    @Override
    public void polymer_addStacks(List<ItemStack> stackList) {
        this.polymer_items.addAll(stackList);
    }

    @Override
    public void polymer_addStack(ItemStack stack) {
        this.polymer_items.add(stack);
    }

    @Override
    public void polymer_clearStacks() {
        this.polymer_items.clear();
    }

    @Override
    public Collection<ItemStack> polymer_getStacks() {
        return this.polymer_items;
    }

    @Override
    public void polymer_removeStacks(Collection<ItemStack> stacks) {
        this.polymer_items.removeAll(stacks);
    }
}
