package eu.pb4.polymer.api.item;

import com.google.common.collect.Multimap;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.events.BooleanEvent;
import eu.pb4.polymer.api.utils.events.ContextAwareModifyEvent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PolymerItemUtils {
    public static final String VIRTUAL_ITEM_ID = "Polymer$itemId";
    public static final String REAL_TAG = "Polymer$itemTag";

    public static final Style CLEAN_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);
    public static final Style NON_ITALIC_STYLE = Style.EMPTY.withItalic(false);

    /**
     * Allows to force rendering of some items as polymer one (for example vanilla ones)
     */
    public static final BooleanEvent<ItemStack> ITEM_CHECK = new BooleanEvent<>();

    /**
     * Allows to modify how virtual items looks before being send to client (only if using build in methods!)
     * It can modify virtual version directly, as long as it's returned at the end.
     * You can also return new ItemStack, however please keep previous nbt so other modifications aren't removed if not needed!
     */
    public static final ContextAwareModifyEvent<ItemStack> ITEM_MODIFICATION_EVENT = new ContextAwareModifyEvent<>();

    /**
     * This methods creates a client side ItemStack representation
     *
     * @param itemStack Server side ItemStack
     * @param player Player being send to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, ServerPlayerEntity player) {
        if (itemStack.getItem() instanceof PolymerItem item) {
            return item.getPolymerItemStack(itemStack, player);
        } else if (itemStack.hasEnchantments()) {
            for (NbtElement enchantment : itemStack.getEnchantments()) {
                String id = ((NbtCompound) enchantment).getString("id");

                Enchantment ench = Registry.ENCHANTMENT.get(Identifier.tryParse(id));

                if (ench instanceof PolymerObject) {
                    return createItemStack(itemStack, player);
                }
            }
        } else if (itemStack.hasNbt() && itemStack.getNbt().contains(EnchantedBookItem.STORED_ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            for (NbtElement enchantment : itemStack.getNbt().getList(EnchantedBookItem.STORED_ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE)) {
                String id = ((NbtCompound) enchantment).getString("id");

                Enchantment ench = Registry.ENCHANTMENT.get(Identifier.tryParse(id));

                if (ench instanceof PolymerObject) {
                    return createItemStack(itemStack, player);
                }
            }
        }

        if (ITEM_CHECK.invoke(itemStack)) {
            return createItemStack(itemStack, player);
        }

        return itemStack;
    }

    /**
     * This method gets real ItemStack from Virtual/Client side one
     * @param itemStack Client side ItemStack
     * @return Server side ItemStack
     */
    public static ItemStack getRealItemStack(ItemStack itemStack) {
        ItemStack out = itemStack;

        if (itemStack.hasNbt()) {
            String id = itemStack.getNbt().getString(VIRTUAL_ITEM_ID);
            if (id != null && !id.isEmpty()) {
                try {
                    Identifier identifier = Identifier.tryParse(id);
                    Item item = Registry.ITEM.get(identifier);
                    out = new ItemStack(item, itemStack.getCount());
                    NbtCompound tag = itemStack.getSubNbt(REAL_TAG);
                    if (tag != null) {
                        out.setNbt(tag);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return out;
    }

    /**
     * This method creates minimal representation of ItemStack
     *
     * @param itemStack Server side ItemStack
     * @param player Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createMinimalItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        Item item = itemStack.getItem();
        int cmd = -1;
        if (itemStack.getItem() instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, player);
            item = data.item();
            cmd = data.cmd();
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());

        if (itemStack.getNbt() != null) {
            out.getOrCreateNbt().put(PolymerItemUtils.REAL_TAG, itemStack.getNbt());
        }

        out.getOrCreateNbt().putString(PolymerItemUtils.VIRTUAL_ITEM_ID, Registry.ITEM.getId(itemStack.getItem()).toString());

        if (cmd != -1) {
            out.getOrCreateNbt().putInt("CustomModelData", cmd);
        }

        return out;
    }

    /**
     * This method creates full (vanilla like) representation of ItemStack
     *
     * @param itemStack Server side ItemStack
     * @param player Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        Item item = itemStack.getItem();
        int cmd = -1;
        if (itemStack.getItem() instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, player);
            item = data.item();
            cmd = data.cmd();
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());

        out.getOrCreateNbt().putString(PolymerItemUtils.VIRTUAL_ITEM_ID, Registry.ITEM.getId(itemStack.getItem()).toString());
        out.getOrCreateNbt().putInt("HideFlags", 127);

        NbtList lore = new NbtList();

        if (itemStack.getNbt() != null) {
            out.getOrCreateNbt().put(PolymerItemUtils.REAL_TAG, itemStack.getNbt());
            assert out.getNbt() != null;
            cmd = itemStack.getNbt().contains("CustomModelData") ? itemStack.getNbt().getInt("CustomModelData") : cmd;

            int dmg = itemStack.getDamage();
            if (dmg != 0) {
                out.getNbt().putInt("Damage", (int) ((((double) dmg) / itemStack.getItem().getMaxDamage()) * item.getMaxDamage()));
            }

            if (itemStack.hasEnchantments()) {
                out.addEnchantment(Enchantments.VANISHING_CURSE, 0);
            }

            NbtElement canDestroy = itemStack.getNbt().get("CanDestroy");

            if (canDestroy != null) {
                out.getNbt().put("CanDestroy", canDestroy);
            }

            NbtElement canPlaceOn = itemStack.getNbt().get("CanPlaceOn");

            if (canPlaceOn != null) {
                out.getNbt().put("CanPlaceOn", canPlaceOn);
            }
        } else if (player == null || itemStack.getFrame() == null) {
            out.setCustomName(itemStack.getItem().getName(itemStack).shallowCopy().fillStyle(PolymerItemUtils.NON_ITALIC_STYLE.withColor(itemStack.getRarity().formatting)));
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = itemStack.getAttributeModifiers(slot);
            for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
                out.addAttributeModifier(entry.getKey(), entry.getValue(), slot);
            }
        }

        try {
            List<Text> tooltip = itemStack.getTooltip(player, TooltipContext.Default.NORMAL);
            MutableText name = (MutableText) tooltip.remove(0);

            if (!out.getName().equals(name)) {
                name.setStyle(name.getStyle().withParent(NON_ITALIC_STYLE));
                out.setCustomName(name);
            }


            if (itemStack.getItem() instanceof PolymerItem) {
                ((PolymerItem) itemStack.getItem()).modifyClientTooltip(tooltip, itemStack, player);
            }

            for (Text t : tooltip) {
                lore.add(NbtString.of(Text.Serializer.toJson(new LiteralText("").append(t).setStyle(PolymerItemUtils.CLEAN_STYLE))));
            }
        } catch (Exception e) {
            // Fallback for mods that require client side methods
            MutableText name = itemStack.getName().shallowCopy();

            if (!out.getName().equals(name)) {
                name.setStyle(name.getStyle().withParent(NON_ITALIC_STYLE));
                out.setCustomName(name);
            }
        }

        if (lore.size() > 0) {
            out.getOrCreateNbt().getCompound("display").put("Lore", lore);
        }

        if (cmd != -1) {
            out.getOrCreateNbt().putInt("CustomModelData", cmd);
        }

        return ITEM_MODIFICATION_EVENT.invoke(itemStack, out, player);
    }

    /**
     * This method is minimal wrapper around {@link PolymerItem#getPolymerItem(ItemStack, ServerPlayerEntity)} to make sure
     * It gets replaced if it represents other PolymerItem
     *
     * @param item  PolymerItem
     * @param stack Server side ItemStack
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side ItemStack
     */
    public static ItemWithCmd getItemSafely(PolymerItem item, ItemStack stack, @Nullable ServerPlayerEntity player, int maxDistance) {
        Item out = item.getPolymerItem(stack, player);
        PolymerItem lastVirtual = item;

        int req = 0;
        while (out instanceof PolymerItem newItem && newItem != item && req < maxDistance) {
            out = newItem.getPolymerItem(stack, player);
            lastVirtual = newItem;
            req++;
        }
        return new ItemWithCmd(out, lastVirtual.getPolymerCustomModelData(stack, player));
    }

    /**
     * This method is minimal wrapper around {@link PolymerItem#getPolymerItem(ItemStack, ServerPlayerEntity)} to make sure
     * It gets replaced if it represents other PolymerItem
     *
     * @param item  PolymerItem
     * @param stack Server side ItemStack
     * @return Client side ItemStack
     */
    public static ItemWithCmd getItemSafely(PolymerItem item, ItemStack stack, @Nullable ServerPlayerEntity player) {
        return getItemSafely(item, stack, player, PolymerBlockUtils.NESTED_DEFAULT_DISTANCE);
    }

        public record ItemWithCmd(Item item, int cmd) {
    }
}
