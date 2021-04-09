package eu.pb4.polymer.item;

import eu.pb4.polymer.mixin.item.ItemStackAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;

public interface VirtualItem {
    Item getVirtualItem();

    default ItemStack getVirtualItemStack(ItemStack itemStack) {
        ItemStack out = itemStack.copy();

        ((ItemStackAccessor) (Object) out).setItem(this.getVirtualItem());

       if (!out.hasCustomName()) {
           out.setCustomName(this.getVirtualDefaultName());
           out.getOrCreateTag().putBoolean("customNameIsDefault", true);
       }

       out.getOrCreateTag().putString("virtualItemId", Registry.ITEM.getId(itemStack.getItem()).toString());

       return out;
    }

    default Text getVirtualDefaultName() {
        return new TranslatableText(this.getTranslationKey());
    }


    String getTranslationKey();
}
