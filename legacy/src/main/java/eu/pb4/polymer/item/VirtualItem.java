package eu.pb4.polymer.item;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Use {@link eu.pb4.polymer.api.item.PolymerItem} instead
 */
@Deprecated
public interface VirtualItem extends VirtualObject, PolymerItem {
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

    @Override
    default Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return getVirtualItem(itemStack, player);
    }

    default ItemStack getVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return ItemHelper.createBasicVirtualItemStack(itemStack, player);
    }

    @Override
    default ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return getVirtualItemStack(itemStack, player);
    }

    default int getCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return -1;
    }

    @Override
    default int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return getCustomModelData(itemStack, player);
    }

    @Override
    default void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        modifyTooltip(tooltip, stack, player);
    }

    default void modifyTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
    }
}