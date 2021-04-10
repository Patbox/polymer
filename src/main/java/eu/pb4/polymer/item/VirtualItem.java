package eu.pb4.polymer.item;

import eu.pb4.polymer.mixin.item.ItemStackAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;

public interface VirtualItem {
    Item getVirtualItem();

    default ItemStack getVirtualItemStack(ItemStack itemStack) {
        ItemStack out = itemStack.copy();

        ((ItemStackAccessor) (Object) out).setItem(this.getVirtualItem());

        boolean hasTag = out.hasTag();

        out.getOrCreateTag().putBoolean(ItemHelper.NO_TAG, !hasTag);

        out.getOrCreateTag().putBoolean(ItemHelper.REMOVE_DISPLAY, out.getSubTag("display") == null);

       if (!out.hasCustomName()) {
           out.setCustomName(this.getVirtualDefaultName());
           out.getOrCreateTag().putBoolean(ItemHelper.REMOVE_CUSTOM_NAME, true);
       }

       out.getOrCreateTag().putString(ItemHelper.VIRTUAL_ITEM_ID, Registry.ITEM.getId(itemStack.getItem()).toString());

       return out;
    }

    default Text getVirtualDefaultName() {
        return new TranslatableText(this.getTranslationKey()).fillStyle(Style.EMPTY.withItalic(false));
    }


    String getTranslationKey();

    default void clearVirtualNBT(ItemStack itemStack) {}
}
