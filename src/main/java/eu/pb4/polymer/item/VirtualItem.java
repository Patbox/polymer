package eu.pb4.polymer.item;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Map;

public interface VirtualItem {
    Item getVirtualItem();

    default ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        ItemStack out = new ItemStack(this.getVirtualItem(), itemStack.getCount());

        out.getOrCreateTag().putString(ItemHelper.VIRTUAL_ITEM_ID, Registry.ITEM.getId(itemStack.getItem()).toString());
        out.getOrCreateTag().putInt("HideFlags", 127);

        ListTag lore = new ListTag();

        if (itemStack.hasTag()) {
            out.getOrCreateTag().put(ItemHelper.REAL_TAG, itemStack.getTag());

            if (!out.hasCustomName()) {
                out.setCustomName(itemStack.getItem().getName(itemStack).shallowCopy().fillStyle(ItemHelper.NON_ITALIC_STYLE.withColor(itemStack.getRarity().formatting)));
            } else {
                out.setCustomName(itemStack.getName());
            }

            int dmg = itemStack.getDamage();
            if (dmg != 0 && out.getTag() != null) {
                out.getTag().putInt("Damage", (int) ((((double) dmg) / itemStack.getItem().getMaxDamage()) * this.getVirtualItem().getMaxDamage()));
            }

            if (itemStack.hasEnchantments()) {
                out.addEnchantment(Enchantments.VANISHING_CURSE, 0);
            }

            Tag canDestroy = itemStack.getTag().get("CanDestroy");

            if (canDestroy != null) {
                out.getTag().put("CanDestroy", canDestroy);
            }

            Tag canPlaceOn = itemStack.getTag().get("CanPlaceOn");

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


        List<Text> tooltip = ItemHelper.buildTooltip(itemStack, player);

        this.addTextToTooltip(tooltip, itemStack, player);

        for (Text t : tooltip) {
            lore.add(StringTag.of(Text.Serializer.toJson(new LiteralText("").append(t).setStyle(ItemHelper.CLEAN_STYLE))));
        }

        if (lore.size() > 0) {
            out.getOrCreateTag().getCompound("display").put("Lore", lore);
        }
        return out;
    }

    default void addTextToTooltip(List<Text> tooltip, ItemStack stack, ServerPlayerEntity player) {}
}
