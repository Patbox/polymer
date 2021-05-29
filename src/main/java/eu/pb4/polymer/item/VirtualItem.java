package eu.pb4.polymer.item;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface used for creation of server-side items
 */
public interface VirtualItem extends VirtualObject {
    /**
     * Returns main/default item used on client
     *
     * @return Vanilla (or other) Item instance
     */
    Item getVirtualItem();


    /**
     * Method used for creation of client-side ItemStack
     *
     * @param itemStack Server-side ItemStack
     * @param player Player for which it's send
     * @return Client-side ItemStack
     */
    default ItemStack getVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return ItemHelper.createBasicVirtualItemStack(itemStack, player);
    }


    /**
     * This method allows to add (or modify) tooltip text
     *
     * @param tooltip Current tooltip text
     * @param stack Server-side ItemStack
     * @param player Target player
     */
    default void modifyTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        this.addTextToTooltip(tooltip, stack, player);
    }

    /**
     * Use modifyTooltip instead!
     */
    @Deprecated(forRemoval = true)
    default void addTextToTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {}
}