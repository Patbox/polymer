package eu.pb4.polymer.item;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.other.BooleanEvent;
import eu.pb4.polymer.other.ContextAwareModifyEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

/**
 * Use {@link eu.pb4.polymer.api.item.PolymerItemUtils} instead
 */
@Deprecated
public class ItemHelper {
    public static final String VIRTUAL_ITEM_ID = PolymerItemUtils.VIRTUAL_ITEM_ID;
    public static final String REAL_TAG = PolymerItemUtils.REAL_TAG;

    public static final Style CLEAN_STYLE = PolymerItemUtils.CLEAN_STYLE;
    public static final Style NON_ITALIC_STYLE = PolymerItemUtils.NON_ITALIC_STYLE;

    /**
     * Allows to force rendering of some items as virtual (for example vanilla ones)
     */
    public static final BooleanEvent<ItemStack> VIRTUAL_ITEM_CHECK = new BooleanEvent<>();

    /**
     * Allows to modify how virtual items looks before being send to client (only if using build in methods!)
     * It can modify virtual version directly, as long as it's returned at the end.
     * You can also return new ItemStack, however please keep previous nbt so other modifications aren't removed if not needed!
     */
    public static final ContextAwareModifyEvent<ItemStack> VIRTUAL_ITEM_MODIFICATION_EVENT = new ContextAwareModifyEvent<>();

    static {
        PolymerItemUtils.ITEM_CHECK.register((obj) -> VIRTUAL_ITEM_CHECK.invoke(obj));
        PolymerItemUtils.ITEM_MODIFICATION_EVENT.register((obj, obj2, obj3) -> VIRTUAL_ITEM_MODIFICATION_EVENT.invoke(obj, obj2, obj3));
    }

    public static ItemStack getVirtualItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        return PolymerItemUtils.getPolymerItemStack(itemStack, player);
    }

    public static ItemStack getRealItemStack(ItemStack itemStack) {
        return PolymerItemUtils.getRealItemStack(itemStack);
    }

    @Deprecated
    public static ItemStack createMinimalVirtualItemStack(ItemStack itemStack) {
        return createBasicVirtualItemStack(itemStack, null);
    }

    public static ItemStack createMinimalVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerItemUtils.createMinimalItemStack(itemStack, player);
    }

    public static ItemStack createBasicVirtualItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerItemUtils.createItemStack(itemStack, player);
    }


    public static ItemWithCmd getItemSafely(VirtualItem item, ItemStack stack, @Nullable ServerPlayerEntity player, int maxDistance) {
        Item out = item.getVirtualItem(stack, player);
        VirtualItem lastVirtual = item;

        int req = 0;
        while (out instanceof VirtualItem newItem && newItem != item && req < maxDistance) {
            out = newItem.getVirtualItem(stack, player);
            lastVirtual = newItem;
            req++;
        }
        return new ItemWithCmd(out, lastVirtual.getCustomModelData(stack, player));
    }


    public static ItemWithCmd getItemSafely(VirtualItem item, ItemStack stack, @Nullable ServerPlayerEntity player) {
        return getItemSafely(item, stack, player, BlockHelper.NESTED_DEFAULT_DISTANCE);
    }

    public record ItemWithCmd(Item item, int cmd) { }
}
