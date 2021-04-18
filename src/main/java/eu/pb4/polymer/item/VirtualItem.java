package eu.pb4.polymer.item;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.util.List;

public interface VirtualItem extends VirtualObject {
    Item getVirtualItem();

    default ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        return ItemHelper.createBasicVirtualItemStack(itemStack, player);
    }

    default void addTextToTooltip(List<Text> tooltip, ItemStack stack, ServerPlayerEntity player) {}
}
