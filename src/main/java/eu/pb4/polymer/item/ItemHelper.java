package eu.pb4.polymer.item;

import eu.pb4.polymer.mixin.item.ItemStackAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemHelper {
    public static String VIRTUAL_ITEM_ID = "Polymer$virtualItemId";
    public static String REMOVE_CUSTOM_NAME = "Polymer$removeCustomName";
    public static String REMOVE_LORE = "Polymer$removeLore";
    public static String REMOVE_DISPLAY = "Polymer$removeDisplay";
    public static String NO_TAG = "Polymer$noTag";

    public static ItemStack getVirtualItemStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof VirtualItem) {
            VirtualItem item = (VirtualItem) itemStack.getItem();

            return item.getVirtualItemStack(itemStack);
        }

        return itemStack;
    }

    public static ItemStack getRealItemStack(ItemStack itemStack) {
        ItemStack out = itemStack.copy();

        String id = out.getOrCreateTag().getString(VIRTUAL_ITEM_ID);
        boolean clearName = out.getOrCreateTag().getBoolean(REMOVE_CUSTOM_NAME);
        boolean clearDisplay = out.getOrCreateTag().getBoolean(REMOVE_DISPLAY);
        boolean noTag = out.getOrCreateTag().getBoolean(NO_TAG);
        boolean clearLore = out.getOrCreateTag().getBoolean(REMOVE_LORE);

        if (id != null) {
            try {
                out.getTag().remove(VIRTUAL_ITEM_ID);
                out.getTag().remove(REMOVE_CUSTOM_NAME);
                out.getTag().remove(REMOVE_DISPLAY);
                out.getTag().remove(NO_TAG);

                Identifier identifier = Identifier.tryParse(id);

                Item item = Registry.ITEM.get(identifier);

                if (item != null && item instanceof VirtualItem) {
                    ((ItemStackAccessor) (Object) out).setItem(item);

                    if (noTag) {
                        out.setTag(null);
                    } else {
                        if (clearDisplay) {
                            out.getOrCreateTag().remove("display");
                        } else {
                            CompoundTag compoundTag = out.getSubTag("display");
                            if (compoundTag != null) {
                                if (clearName) {
                                    if (compoundTag.contains("Name")) {
                                        compoundTag.remove("Name");
                                    }
                                }

                                if (clearLore) {
                                    if (compoundTag.contains("Lore")) {
                                        compoundTag.remove("Lore");
                                    }
                                }
                            }

                            ((VirtualItem) item).clearVirtualNBT(out);
                        }
                    }
                }

            } catch (Exception e) {
            }
        }

        return out;
    }
}
