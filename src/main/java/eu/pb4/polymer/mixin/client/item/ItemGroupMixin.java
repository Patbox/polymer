package eu.pb4.polymer.mixin.client.item;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
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

@Environment(EnvType.CLIENT)
@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin implements ClientItemGroupExtension {
    @Shadow private ItemStack icon;

    @Shadow public abstract String getName();

    @Shadow @Final public static ItemGroup SEARCH;
    @Unique private List<ItemStack> polymer_items = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_storeInMap(int index, String id, CallbackInfo ci) {
        if (!(this instanceof PolymerObject)) {
            InternalClientRegistry.VANILLA_ITEM_GROUPS.put(id, (ItemGroup) (Object) this);
        }
    }

    @Inject(method = "getIcon", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemGroup;createIcon()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void polymer_wrapIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (this.icon.getItem() instanceof PolymerItem virtualItem) {
            this.icon = virtualItem.getPolymerItemStack(this.icon, null);
        }
    }

    @Inject(method = "appendStacks", at = @At("TAIL"))
    private void polymer_appendStacks(DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (ClientUtils.isClientSide()) {
            stacks.removeIf((s) -> PolymerItemUtils.isPolymerServerItem(s));
            if (((Object) this) == ItemGroup.SEARCH) {
                for (var group : ItemGroup.GROUPS) {
                    if (group instanceof InternalClientItemGroup clientItemGroup) {
                        stacks.addAll(clientItemGroup.getStacks());
                    } else {
                        stacks.addAll(((ClientItemGroupExtension) group).polymer_getStacks());
                    }
                }
            } else {
                stacks.addAll(this.polymer_items);
            }
        }
    }

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
