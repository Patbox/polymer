package eu.pb4.polymer.core.api.item;

import com.google.common.collect.Multimap;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.FunctionEvent;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class PolymerItemUtils {
    public static final String POLYMER_ITEM_ID = "Polymer$itemId";
    public static final String REAL_TAG = "Polymer$itemTag";
    public static final String ITEM_FRAME_NAME_TAG = "Polymer$itemName";

    public static final Style CLEAN_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);
    public static final Style NON_ITALIC_STYLE = Style.EMPTY.withItalic(false);

    /**
     * Allows to force rendering of some items as polymer one (for example vanilla ones)
     */
    public static final BooleanEvent<Predicate<ItemStack>> ITEM_CHECK = new BooleanEvent<>();
    /**
     * Allows to modify how virtual items looks before being send to client (only if using build in methods!)
     * It can modify virtual version directly, as long as it's returned at the end.
     * You can also return new ItemStack, however please keep previous nbt so other modifications aren't removed if not needed!
     */
    public static final FunctionEvent<ItemModificationEventHandler, ItemStack> ITEM_MODIFICATION_EVENT = new FunctionEvent<>();

    private PolymerItemUtils() {
    }

    /**
     * This methods creates a client side ItemStack representation
     *
     * @param itemStack Server side ItemStack
     * @param player    Player being send to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return getPolymerItemStack(itemStack, PolymerUtils.getTooltipContext(player), player);
    }

    /**
     * This methods creates a client side ItemStack representation
     *
     * @param itemStack      Server side ItemStack
     * @param tooltipContext Tooltip Context
     * @param player         Player being send to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext tooltipContext, @Nullable ServerPlayerEntity player) {
        if (getPolymerIdentifier(itemStack) != null) {
            return itemStack;
        } else if (itemStack.getItem() instanceof PolymerItem item) {
            return item.getPolymerItemStack(itemStack, tooltipContext, player);
        } else if (shouldPolymerConvert(itemStack, player)) {
            return createItemStack(itemStack, tooltipContext, player);
        }

        if (ITEM_CHECK.invoke((x) -> x.test(itemStack))) {
            return createItemStack(itemStack, player);
        }

        return itemStack;
    }

    /**
     * This method gets real ItemStack from Virtual/Client side one
     *
     * @param itemStack Client side ItemStack
     * @return Server side ItemStack
     */
    public static ItemStack getRealItemStack(ItemStack itemStack) {
        ItemStack out = itemStack;

        if (itemStack.hasNbt()) {
            String id = itemStack.getNbt().getString(POLYMER_ITEM_ID);
            if (id != null && !id.isEmpty()) {
                try {
                    Identifier identifier = Identifier.tryParse(id);
                    Item item = Registries.ITEM.get(identifier);
                    if (item != Items.AIR) {
                        out = new ItemStack(item, itemStack.getCount());
                        NbtCompound tag = itemStack.getSubNbt(REAL_TAG);
                        if (tag != null) {
                            out.setNbt(tag);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return out;
    }

    /**
     * Returns stored identifier of Polymer ItemStack. If it's invalid, null is returned instead.
     */
    @Nullable
    public static Identifier getPolymerIdentifier(ItemStack itemStack) {
        if (itemStack.hasNbt()) {
            return getIdentifierFrom(itemStack.getNbt(), POLYMER_ITEM_ID);
        }

        return null;
    }

    /**
     * Returns stored identifier of Polymer/other supported server mod ItemStack. If it's invalid, null is returned instead.
     */
    @Nullable
    public static Identifier getServerIdentifier(ItemStack itemStack) {
        if (itemStack.hasNbt()) {
            var id = getIdentifierFrom(itemStack.getNbt(), POLYMER_ITEM_ID);

            if (id == null) {
                id = getIdentifierFrom(itemStack.getNbt(), "PolyMcId");
            }

            return id;
        }

        return null;
    }

    @Nullable
    private static Identifier getIdentifierFrom(NbtCompound compound, String nbtKey) {
        String id = compound.getString(nbtKey);
        if (id != null && !id.isEmpty()) {
            return Identifier.tryParse(id);
        }
        return null;
    }

    @Nullable
    public static NbtCompound getPolymerNbt(ItemStack itemStack) {
        if (getPolymerIdentifier(itemStack) != null) {
            if (itemStack.getNbt().contains(REAL_TAG, NbtElement.COMPOUND_TYPE)) {
                return itemStack.getNbt().getCompound(REAL_TAG);
            }
        }

        return null;
    }

    public static boolean isPolymerServerItem(ItemStack itemStack) {
        return shouldPolymerConvert(itemStack, null);
    }

    private static boolean shouldPolymerConvert(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (getPolymerIdentifier(itemStack) != null) {
            return false;
        }
        if (itemStack.getItem() instanceof PolymerItem) {
            return true;
        } else if (itemStack.hasNbt()) {
            if (itemStack.hasEnchantments()) {
                for (NbtElement enchantment : itemStack.getEnchantments()) {
                    String id = ((NbtCompound) enchantment).getString("id");

                    Enchantment ench = Registries.ENCHANTMENT.get(Identifier.tryParse(id));

                    if (ench instanceof PolymerObject) {
                        if (ench instanceof PolymerSyncedObject polymerEnchantment && polymerEnchantment.getPolymerReplacement(player) == ench) {
                            continue;
                        }

                        return true;
                    }
                }
            } else if (itemStack.getNbt().contains(EnchantedBookItem.STORED_ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
                for (NbtElement enchantment : itemStack.getNbt().getList(EnchantedBookItem.STORED_ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE)) {
                    String id = ((NbtCompound) enchantment).getString("id");

                    Enchantment ench = Registries.ENCHANTMENT.get(Identifier.tryParse(id));

                    if (ench instanceof PolymerObject) {
                        if (ench instanceof PolymerSyncedObject polymerEnchantment && polymerEnchantment.getPolymerReplacement(player) == ench) {
                            continue;
                        }

                        return true;
                    }
                }
            } else if (itemStack.getItem() instanceof PotionItem) {
                for (StatusEffectInstance statusEffectInstance : PotionUtil.getPotionEffects(itemStack)) {
                    if (statusEffectInstance.getEffectType() instanceof PolymerObject) {
                        return true;
                    }
                }
            }

            if (itemStack.getNbt().contains("ChargedProjectiles", NbtElement.LIST_TYPE)) {
                for (var itemNbt : itemStack.getNbt().getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE)) {
                    if (shouldPolymerConvert(ItemStack.fromNbt((NbtCompound) itemNbt), player)) {
                        return true;
                    }
                }
            }

            if (itemStack.getNbt().contains("Items", NbtElement.LIST_TYPE)) {
                for (var itemNbt : itemStack.getNbt().getList("Items", NbtElement.COMPOUND_TYPE)) {
                    if (shouldPolymerConvert(ItemStack.fromNbt((NbtCompound) itemNbt), player)) {
                        return true;
                    }
                }
            }

            if (CompatStatus.POLYMER_RESOURCE_PACK) {
                var display = itemStack.getSubNbt("display");
                if (display != null && display.contains("color", NbtElement.INT_TYPE)) {
                    var color = display.getInt("color");
                    return PolymerResourcePackUtils.isColorTaken(color);
                }
            }
        }


        return false;
    }

    /**
     * This method creates minimal representation of ItemStack
     *
     * @param itemStack Server side ItemStack
     * @param player    Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createMinimalItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        Item item = itemStack.getItem();
        int cmd = -1;
        if (itemStack.getItem() instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, player);
            item = data.item();
            cmd = data.customModelData();
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());

        if (itemStack.getNbt() != null) {
            out.getOrCreateNbt().put(PolymerItemUtils.REAL_TAG, itemStack.getNbt());
        }

        out.getOrCreateNbt().putString(PolymerItemUtils.POLYMER_ITEM_ID, Registries.ITEM.getId(itemStack.getItem()).toString());

        if (cmd != -1) {
            out.getOrCreateNbt().putInt("CustomModelData", cmd);
        }

        return out;
    }

    public static int getSafeColor(int inputColor) {
        if (inputColor % 2 == 1) {
            return Math.max(0, inputColor - 1);
        }
        return inputColor;
    }

    /**
     * This method creates full (vanilla like) representation of ItemStack
     *
     * @param itemStack Server side ItemStack
     * @param player    Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return createItemStack(itemStack, PolymerUtils.getTooltipContext(player), player);
    }

    /**
     * This method creates full (vanilla like) representation of ItemStack
     *
     * @param itemStack      Server side ItemStack
     * @param tooltipContext TooltipContext
     * @param player         Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createItemStack(ItemStack itemStack, TooltipContext tooltipContext, @Nullable ServerPlayerEntity player) {
        Item item = itemStack.getItem();
        int cmd = -1;
        int color = -1;
        if (itemStack.getItem() instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, player);
            item = data.item();
            cmd = data.customModelData();
            color = data.color();
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());

        out.getOrCreateNbt().putString(PolymerItemUtils.POLYMER_ITEM_ID, Registries.ITEM.getId(itemStack.getItem()).toString());
        out.getNbt().putInt("HideFlags", 127);

        NbtList lore = new NbtList();

        if (itemStack.getNbt() != null) {
            out.getNbt().put(PolymerItemUtils.REAL_TAG, itemStack.getNbt());
            assert out.getNbt() != null;
            cmd = cmd == -1 && itemStack.getNbt().contains("CustomModelData") ? itemStack.getNbt().getInt("CustomModelData") : cmd;

            if (color == -1 && itemStack.getNbt().contains("display", NbtElement.COMPOUND_TYPE)) {
                var display = itemStack.getSubNbt("display");
                if (display.contains("color", NbtElement.INT_TYPE)) {
                    color = display.getInt("color");

                    if (color % 2 == 1) {
                        color = Math.max(0, color - 1);
                    }
                }
            }

            int dmg = itemStack.getDamage();
            if (dmg != 0) {
                out.getNbt().putInt("Damage", (int) ((((double) dmg) / itemStack.getItem().getMaxDamage()) * item.getMaxDamage()));
            }

            if (itemStack.hasEnchantments()) {
                var list = new NbtList();
                for (var enchNbt : itemStack.getEnchantments()) {
                    var ench = Registries.ENCHANTMENT.get(EnchantmentHelper.getIdFromNbt((NbtCompound) enchNbt));

                    if (ench instanceof PolymerSyncedObject polyEnch) {
                        var possible = (Enchantment) polyEnch.getPolymerReplacement(player);

                        if (possible != null) {
                            list.add(EnchantmentHelper.createNbt(Registries.ENCHANTMENT.getId(possible), EnchantmentHelper.getLevelFromNbt((NbtCompound) enchNbt)));
                        }
                    } else if (!(ench instanceof PolymerObject)) {
                        list.add(enchNbt.copy());
                    }
                }

                if (list.isEmpty()) {
                    list.add(new NbtCompound());
                }

                out.getNbt().put(ItemStack.ENCHANTMENTS_KEY, list);
            } else if (itemStack.hasGlint()) {
                var list = new NbtList();
                list.add(new NbtCompound());
                out.getNbt().put(ItemStack.ENCHANTMENTS_KEY, list);
            }

            if (itemStack.getItem() instanceof PotionItem) {
                if (!out.getOrCreateNbt().contains("CustomPotionColor")) {
                    out.getOrCreateNbt().putInt("CustomPotionColor", PotionUtil.getColor(itemStack));
                }
            }

            NbtElement canDestroy = itemStack.getNbt().get("CanDestroy");

            if (canDestroy != null) {
                out.getNbt().put("CanDestroy", canDestroy);
            }

            NbtElement canPlaceOn = itemStack.getNbt().get("CanPlaceOn");

            if (canPlaceOn != null) {
                out.getNbt().put("CanPlaceOn", canPlaceOn);
            }

            if (CrossbowItem.isCharged(itemStack)) {
                CrossbowItem.setCharged(out, true);
            }

            var beTag = itemStack.getNbt().get(BlockItem.BLOCK_ENTITY_TAG_KEY);

            if (beTag != null) {
                out.getNbt().put(BlockItem.BLOCK_ENTITY_TAG_KEY, beTag);
            }

            try {
                if (itemStack.getNbt().contains("ChargedProjectiles", NbtElement.LIST_TYPE)) {
                    var outList = new NbtList();

                    for (var itemNbt : itemStack.getNbt().getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE)) {
                        outList.add(getPolymerItemStack(ItemStack.fromNbt((NbtCompound) itemNbt), tooltipContext, player).writeNbt(new NbtCompound()));
                    }

                    out.getNbt().put("ChargedProjectiles", outList);
                }
            } catch (Throwable e) {
                if (PolymerImpl.LOG_MORE_ERRORS) {
                    e.printStackTrace();
                }
            }

            try {
                if (itemStack.getNbt().contains("Items", NbtElement.LIST_TYPE)) {
                    var outList = new NbtList();

                    for (var itemNbt : itemStack.getNbt().getList("Items", NbtElement.COMPOUND_TYPE)) {
                        var base = new NbtCompound();
                        var slot = ((NbtCompound) itemNbt).get("Slot");
                        if (slot != null) {
                            base.put("Slot", slot);
                        }
                        outList.add(getPolymerItemStack(ItemStack.fromNbt((NbtCompound) itemNbt), tooltipContext, player).writeNbt(base));
                    }

                    out.getNbt().put("Items", outList);
                }
            } catch (Throwable e) {
                if (PolymerImpl.LOG_MORE_ERRORS) {
                    e.printStackTrace();
                }
            }
        } else {
            if (itemStack.hasGlint()) {
                var list = new NbtList();
                list.add(new NbtCompound());
                out.getNbt().put(ItemStack.ENCHANTMENTS_KEY, list);
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = itemStack.getAttributeModifiers(slot);
            for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : multimap.entries()) {
                out.addAttributeModifier(entry.getKey(), entry.getValue(), slot);
            }
        }

        try {
            List<Text> tooltip = itemStack.getTooltip(player, tooltipContext);
            MutableText name = (MutableText) tooltip.remove(0);

            if (!out.getName().equals(name)) {
                name.setStyle(name.getStyle().withParent(NON_ITALIC_STYLE));
                out.setCustomName(name);
            }


            if (itemStack.getItem() instanceof PolymerItem) {
                ((PolymerItem) itemStack.getItem()).modifyClientTooltip(tooltip, itemStack, player);
            }

            for (Text t : tooltip) {
                lore.add(NbtString.of(Text.Serializer.toJson(Text.empty().append(t).setStyle(PolymerItemUtils.CLEAN_STYLE))));
            }
        } catch (Throwable e) {
            if (PolymerImpl.LOG_MORE_ERRORS) {
                PolymerImpl.LOGGER.error("Failed to get tooltip of " + itemStack, e);
            }

            // Fallback for mods that require client side methods for tooltips
            try {
                MutableText name = itemStack.getName().copy();

                if (!out.getName().equals(name)) {
                    name.setStyle(name.getStyle().withParent(NON_ITALIC_STYLE));
                    out.setCustomName(name);
                }
            } catch (Throwable e2) {
                // Fallback for mods that can't even handle names correctly...
                // Do nothing and hope for the best™

                if (PolymerImpl.LOG_MORE_ERRORS) {
                    PolymerImpl.LOGGER.error("Failed for second time. Ignoring.", e2);

                }
            }
        }
        var outNbt = out.getOrCreateNbt();

        if (lore.size() > 0) {
            outNbt.getCompound("display").put("Lore", lore);
        }
        if (color != -1) {
            outNbt.getCompound("display").putInt("color", color);
        }
        if (cmd != -1) {
            outNbt.putInt("CustomModelData", cmd);
        }

        return ITEM_MODIFICATION_EVENT.invoke((col) -> {
            var custom = out;

            for (var in : col) {
                custom = in.modifyItem(itemStack, custom, player);
            }

            return custom;
        });
    }

    /**
     * This method is minimal wrapper around {@link PolymerItem#getPolymerItem(ItemStack, ServerPlayerEntity)} to make sure
     * It gets replaced if it represents other PolymerItem
     *
     * @param item        PolymerItem
     * @param stack       Server side ItemStack
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side ItemStack
     */
    public static ItemWithMetadata getItemSafely(PolymerItem item, ItemStack stack, @Nullable ServerPlayerEntity player, int maxDistance) {
        Item out = item.getPolymerItem(stack, player);
        PolymerItem lastVirtual = item;

        int req = 0;
        while (out instanceof PolymerItem newItem && newItem != item && req < maxDistance) {
            out = newItem.getPolymerItem(stack, player);
            lastVirtual = newItem;
            req++;
        }
        return new ItemWithMetadata(out, lastVirtual.getPolymerCustomModelData(stack, player), lastVirtual.getPolymerArmorColor(stack, player));
    }

    /**
     * This method is minimal wrapper around {@link PolymerItem#getPolymerItem(ItemStack, ServerPlayerEntity)} to make sure
     * It gets replaced if it represents other PolymerItem
     *
     * @param item  PolymerItem
     * @param stack Server side ItemStack
     * @return Client side ItemStack
     */
    public static ItemWithMetadata getItemSafely(PolymerItem item, ItemStack stack, @Nullable ServerPlayerEntity player) {
        return getItemSafely(item, stack, player, PolymerBlockUtils.NESTED_DEFAULT_DISTANCE);
    }

    @FunctionalInterface
    public interface ItemModificationEventHandler {
        ItemStack modifyItem(ItemStack original, ItemStack client, ServerPlayerEntity player);
    }

    public record ItemWithMetadata(Item item, int customModelData, int color) {
    }
}
