package eu.pb4.polymer.item;

import com.google.common.collect.Multimap;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ItemHelper {
    public static final String VIRTUAL_ITEM_ID = "Polymer$itemId";
    public static final String REAL_TAG = "Polymer$itemTag";

    public static final Style CLEAN_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);
    public static final Style NON_ITALIC_STYLE = Style.EMPTY.withItalic(false);

    public static ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        if (itemStack.getItem() instanceof VirtualItem) {
            VirtualItem item = (VirtualItem) itemStack.getItem();
            return item.getVirtualItemStack(itemStack, player);
        } else if (itemStack.hasEnchantments()) {
            for (NbtElement enchantment : itemStack.getEnchantments()) {
                String id = ((NbtCompound) enchantment).getString("id");

                Enchantment ench = Registry.ENCHANTMENT.get(Identifier.tryParse(id));

                if (ench instanceof VirtualObject) {
                    return createBasicVirtualItemStack(itemStack, player);
                }
            }
        }

        return itemStack;
    }

    public static ItemStack getRealItemStack(ItemStack itemStack) {
        ItemStack out = itemStack;

        if (itemStack.hasTag()) {
            String id = itemStack.getTag().getString(VIRTUAL_ITEM_ID);
            if (id != null && !id.isEmpty()) {
                try {
                    Identifier identifier = Identifier.tryParse(id);
                    Item item = Registry.ITEM.get(identifier);
                    out = new ItemStack(item, itemStack.getCount());
                    NbtCompound tag = itemStack.getSubTag(REAL_TAG);
                    if (tag != null) {
                        out.setTag(tag);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return out;
    }

    public static ItemStack createMinimalVirtualItemStack(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (itemStack.getItem() instanceof VirtualItem) {
            item = ((VirtualItem) itemStack.getItem()).getVirtualItem();
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());

        if (itemStack.getTag() != null) {
            out.getOrCreateTag().put(ItemHelper.REAL_TAG, itemStack.getTag());
        }

        out.getOrCreateTag().putString(ItemHelper.VIRTUAL_ITEM_ID, Registry.ITEM.getId(itemStack.getItem()).toString());

        return out;
    }

    public static ItemStack createBasicVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        Item item = itemStack.getItem();
        if (itemStack.getItem() instanceof VirtualItem) {
            item = ((VirtualItem) itemStack.getItem()).getVirtualItem();
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());

        out.getOrCreateTag().putString(ItemHelper.VIRTUAL_ITEM_ID, Registry.ITEM.getId(itemStack.getItem()).toString());
        out.getOrCreateTag().putInt("HideFlags", 127);

        NbtList lore = new NbtList();

        if (itemStack.getTag() != null) {
            out.getOrCreateTag().put(ItemHelper.REAL_TAG, itemStack.getTag());
            assert out.getTag() != null;

            if (!itemStack.hasCustomName()) {
                out.setCustomName(itemStack.getItem().getName(itemStack).shallowCopy().fillStyle(ItemHelper.NON_ITALIC_STYLE.withColor(itemStack.getRarity().formatting)));
            } else {
                out.setCustomName(itemStack.getName());
            }

            int dmg = itemStack.getDamage();
            if (dmg != 0) {
                out.getTag().putInt("Damage", (int) ((((double) dmg) / itemStack.getItem().getMaxDamage()) * item.getMaxDamage()));
            }

            if (itemStack.hasEnchantments()) {
                out.addEnchantment(Enchantments.VANISHING_CURSE, 0);
            }

            NbtElement canDestroy = itemStack.getTag().get("CanDestroy");

            if (canDestroy != null) {
                out.getTag().put("CanDestroy", canDestroy);
            }

            NbtElement canPlaceOn = itemStack.getTag().get("CanPlaceOn");

            if (canPlaceOn != null) {
                out.getTag().put("CanPlaceOn", canPlaceOn);
            }
        } else {
            out.setCustomName(itemStack.getItem().getName(itemStack).shallowCopy().fillStyle(ItemHelper.NON_ITALIC_STYLE.withColor(itemStack.getRarity().formatting)));
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = itemStack.getAttributeModifiers(slot);
            for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
                out.addAttributeModifier(entry.getKey(), entry.getValue(), slot);
            }
        }


        List<Text> tooltip = itemStack.getTooltip(player, TooltipContext.Default.NORMAL);
        tooltip.remove(0);

        if (itemStack.getItem() instanceof VirtualItem) {
            ((VirtualItem) itemStack.getItem()).modifyTooltip(tooltip, itemStack, player);
        }

        for (Text t : tooltip) {
            lore.add(NbtString.of(Text.Serializer.toJson(new LiteralText("").append(t).setStyle(ItemHelper.CLEAN_STYLE))));
        }

        if (lore.size() > 0) {
            out.getOrCreateTag().getCompound("display").put("Lore", lore);
        }
        return out;
    }
}
