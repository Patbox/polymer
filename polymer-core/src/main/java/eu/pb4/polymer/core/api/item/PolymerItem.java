package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

/**
 * Interface used for creation of server-side items
 */
public interface PolymerItem extends PolymerSyncedObject<Item> {
    /**
     * Returns main/default item used on client for specific player
     *
     * @param itemStack ItemStack of virtual item
     * @param context    Context for which it's send
     * @return Vanilla (or other) Item instance
     */
    Item getPolymerItem(ItemStack itemStack, PacketContext context);


    default Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return stack.get(DataComponentTypes.ITEM_MODEL);
    }

    /**
     * Method used for creation of client-side ItemStack
     *
     * @param itemStack Server-side ItemStack
     * @param context    Player for which it's send
     * @return Client-side ItemStack
     */
    default ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        return PolymerItemUtils.createItemStack(itemStack, tooltipType, context);
    }

    /**
     * This method allows to modify tooltip text
     * If you just want to add your own one, use {@link Item#appendTooltip(ItemStack, Item.TooltipContext, List, TooltipType)}
     *
     * @param tooltip Current tooltip text
     * @param stack   Server-side ItemStack
     * @param context  Target player
     */
    default void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
    }
    @Override
    default Item getPolymerReplacement(PacketContext context) {
        return this.getPolymerItem(((Item) this).getDefaultStack(), context);
    }

    default boolean handleMiningOnServer(ItemStack tool, BlockState targetBlock, BlockPos pos, ServerPlayerEntity player) {
        return false;
    }

    default boolean shouldStorePolymerItemStackCount() {
        return false;
    }
}