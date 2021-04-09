package eu.pb4.polymer.item;

import com.google.gson.JsonParseException;
import eu.pb4.polymer.mixin.item.ItemStackAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemHelper {
    public static ItemStack getVirtualItemStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof VirtualItem) {
            VirtualItem item = (VirtualItem) itemStack.getItem();

            return item.getVirtualItemStack(itemStack);
        }

        return itemStack;
    }

    public static ItemStack getRealItemStack(ItemStack itemStack) {
        ItemStack out = itemStack.copy();

        String id = out.getOrCreateTag().getString("virtualItemId");
        boolean customName = out.getOrCreateTag().getBoolean("customNameIsDefault");
        if (id != null) {
            try {
                out.getTag().remove("virtualItemId");
                out.getTag().remove("customNameIsDefault");

                Identifier identifier = Identifier.tryParse(id);

                Item item = Registry.ITEM.get(identifier);

                if (item != null && item instanceof VirtualItem) {
                    ((ItemStackAccessor) (Object) out).setItem(item);

                    if (customName) {
                        CompoundTag compoundTag = out.getSubTag("display");
                        if (compoundTag != null && compoundTag.contains("Name", 8)) {
                            compoundTag.remove("Name");
                        }
                    }
                }

            } catch (Exception e) {}
        }

        return out;
    }
}
