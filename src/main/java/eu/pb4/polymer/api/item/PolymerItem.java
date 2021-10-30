package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface used for creation of server-side items
 */
public interface PolymerItem extends PolymerObject {
    /**
     * Returns main/default item used on client for specific player
     *
     * @param itemStack ItemStack of virtual item
     * @param player    Player for which it's send
     * @return Vanilla (or other) Item instance
     */
    Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player);

    /**
     * Method used for creation of client-side ItemStack
     *
     * @param itemStack Server-side ItemStack
     * @param player    Player for which it's send
     * @return Client-side ItemStack
     */
    default ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerItemUtils.createItemStack(itemStack, player);
    }


    /**
     * Method used for getting custom model data of items
     *
     * @param itemStack Server-side ItemStack
     * @param player    Player for which it's send
     * @return Custom model data or -1 if not present
     */
    default int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return -1;
    }


    /**
     * This method allows to modify tooltip text
     * If you just want to add your own one, use {@link Item#appendTooltip(ItemStack, World, List, TooltipContext)}
     *
     * @param tooltip Current tooltip text
     * @param stack   Server-side ItemStack
     * @param player  Target player
     */
    default void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
    }
}