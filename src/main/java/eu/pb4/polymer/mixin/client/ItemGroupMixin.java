package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.interfaces.ClientItemGroupExtension;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemGroup.class)
public class ItemGroupMixin implements ClientItemGroupExtension {
    @Shadow private ItemStack icon;
    @Unique private List<ItemStack> polymer_items = new ArrayList<>();

    @Inject(method = "getIcon", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemGroup;createIcon()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void polymer_wrapIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (this.icon.getItem() instanceof PolymerItem virtualItem) {
            this.icon = virtualItem.getPolymerItemStack(this.icon, null);
        }
    }

    @Inject(method = "appendStacks", at = @At("TAIL"))
    private void polymer_appendStacks(DefaultedList<ItemStack> stacks, CallbackInfo ci) {
        if (ClientUtils.isClientSide()) {
            stacks.addAll(this.polymer_items);
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
}
