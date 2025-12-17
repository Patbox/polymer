package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Interface used for conversion of server items to their client side counterparts
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

    /**
     * Method used for creation of client-side ItemStack
     *
     * @param itemStack Server-side ItemStack
     * @param context    Player for which it's send
     * @return Client-side ItemStack
     */
    default ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context) {
        return PolymerItemUtils.createItemStack(itemStack, tooltipType, context);
    }

    /**
     * Method used for selecting model to use. Invoked within PolymerItemUtils#createItemStack / default stack creation
     * before polymer-specific modifications
     *
     * @param stack Server-side ItemStack, used as reference
     * @param context    Player for which it's send
     * @return Identifier targetting item model or null to fallback to base item one
     */
    @Nullable
    default Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return stack.get(DataComponents.ITEM_MODEL);
    }

    /**
     * Method used for creation of client-side ItemStack.
     * Invoked within PolymerItemUtils#createItemStack / default stack creation before polymer-specific modifications.
     * For modifying after polymer, you should override {@link PolymerItem#getPolymerItemStack(ItemStack, TooltipFlag, PacketContext)}.
     *
     * @param out Client-side ItemStack, sent to the player (and one that should be modified)
     * @param stack Server-side ItemStack, used as reference
     * @param context    Player for which it's send
     */
    default void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context) {
    }


    /**
     * This method allows to modify tooltip text. Invoked within PolymerItemUtils#createItemStack / default stack creation
     * If you just want to add your own one, use {@link Item#appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer, TooltipFlag)}
     * or Fabric API provided events.
     *
     * @param tooltip Current tooltip text
     * @param stack   Server-side ItemStack
     * @param context  Target player
     */
    default void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
    }

    @Override
    default Item getPolymerReplacement(Item item, PacketContext context) {
        return this.getPolymerItem(item.getDefaultInstance(), context);
    }

    /**
     * Makes the block mining logic run on the server instead of the client. Useful if the tool
     * changes how things are mined outside of vanilla {@link DataComponents#TOOL} component.
     *
     * @param tool tool used
     * @param targetBlock targetted block
     * @param pos block's position
     * @param player user
     * @return true if logic should be handled server side, false otherwise
     */
    default boolean handleMiningOnServer(ItemStack tool, BlockState targetBlock, BlockPos pos, ServerPlayer player) {
        return false;
    }

    /**
     * Makes it so real item count is stored in custom data, instead of client stack
     */
    default boolean shouldStorePolymerItemStackCount() {
        return false;
    }

    /**
     * Allows to run additional logic, making interactions work correctly server side,
     * emulating or preventing otherwise client dictated behaviour.
     *
     * @param state interacted block state
     * @param player the user
     * @param hand hand used for interaction
     * @param stack item stack in hand
     * @param world used world
     * @param blockHitResult hit result used for the interaction
     * @param actionResult the action result caused by such interaction.
     * @return whatever the interaction should be handled as polymer / server side one.
     */
    default boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return false;
    }

    /**
     * Allows to run additional logic, making interactions work correctly server side,
     * emulating or preventing otherwise client dictated behaviour.
     *
     * @param player the user
     * @param hand hand used for interaction
     * @param stack item stack in hand
     * @param world used world
     * @param entity interacted entity
     * @param actionResult the action result caused by such interaction.
     * @return whatever the interaction should be handled as polymer / server side one.
     */
    default boolean isPolymerEntityInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, Entity entity, InteractionResult actionResult) {
        return false;
    }

    /**
     * Allows to run additional logic, making interactions work correctly server side,
     * emulating or preventing otherwise client dictated behaviour.
     *
     * @param player the user
     * @param hand hand used for interaction
     * @param stack item stack in hand
     * @param world used world
     * @param actionResult the action result caused by such interaction.
     * @return whatever the interaction should be handled as polymer / server side one.
     */
    default boolean isPolymerItemInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, InteractionResult actionResult) {
        return true;
    }

    /**
     * Changes sound logic within the block interaction code to always play sounds to the client.
     *
     * @param state interacted block state
     * @param player the user
     * @param hand hand used for interaction
     * @param stack item stack used for block interaction
     * @param world used world
     * @param blockHitResult hit result used for block interaction
     * @return whatever the client only sounds should be played on server.
     */
    default boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return false;
    }

    /**
     * Changes sound logic within the item use interaction code to always play sounds to the client.
     *
     * @param player the user
     * @param hand hand used for interaction
     * @param stack item stack used for block interaction
     * @param world used world
     * @return whatever the client only sounds should be played on server.
     */
    default boolean isIgnoringItemInteractionPlaySoundExceptedEntity(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world) {
        return false;
    }


    /**
     * Makes the target item use polymer logic for conversion, functionally the same as implementing
     * PolymerItem interface on custom Item class.
     *
     * @param item the target item
     * @param polymerItem instance of PolymerItem
     */
    static void registerOverlay(Item item, PolymerItem polymerItem) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.ITEM, item, polymerItem);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.ITEM, item);
    }
}