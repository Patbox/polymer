package eu.pb4.polymer.item;

import eu.pb4.polymer.interfaces.VirtualObject;
import eu.pb4.polymer.resourcepack.CMDInfo;
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


    @Deprecated(forRemoval = true)
    default Item getVirtualItem(@Nullable ServerPlayerEntity player) {
        return this.getVirtualItem();
    }

    /**
     * Returns main/default item used on client for specific player
     *
     * @param itemStack ItemStack of virtual item
     * @param player    Player for which it's send
     * @return Vanilla (or other) Item instance
     */
    default Item getVirtualItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.getVirtualItem(player);
    }

    /**
     * Method used for creation of client-side ItemStack
     *
     * @param itemStack Server-side ItemStack
     * @param player    Player for which it's send
     * @return Client-side ItemStack
     */
    default ItemStack getVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return ItemHelper.createBasicVirtualItemStack(itemStack, player);
    }


    /**
     * Method used for getting custom model data of items
     *
     * @param itemStack Server-side ItemStack
     * @param player    Player for which it's send
     * @return Custom model data or -1 if not present
     */
    default int getCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return -1;
    }


    /**
     * This method allows to add (or modify) tooltip text
     *
     * @param tooltip Current tooltip text
     * @param stack   Server-side ItemStack
     * @param player  Target player
     */
    default void modifyTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
    }
}