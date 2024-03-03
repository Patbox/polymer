package eu.pb4.polymer.core.api.item;

import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.RecordBuilder;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.FunctionEvent;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.mixin.item.ItemEnchantmentsComponentAccessor;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public final class PolymerItemUtils {
    public static final String POLYMER_STACK = "$polymer:stack";
    public static final MapCodec<ItemStack> POLYMER_STACK_CODEC = ItemStack.CODEC.fieldOf(POLYMER_STACK);
    public static final MapCodec<Identifier> POLYMER_STACK_ID_CODEC = Identifier.CODEC.fieldOf("id").fieldOf(POLYMER_STACK);
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
    private static final DataComponentType<?>[] COMPONENTS_TO_COPY = { DataComponentTypes.CAN_BREAK, DataComponentTypes.CAN_BREAK,
            DataComponentTypes.BLOCK_ENTITY_DATA, DataComponentTypes.TRIM,
            DataComponentTypes.LODESTONE_TARGET
    };

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
        } else if (isPolymerServerItem(itemStack, player)) {
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
        var custom = itemStack.get(DataComponentTypes.CUSTOM_DATA);

        if (custom != null && custom.contains(POLYMER_STACK)) {
            try {
                return custom.get(POLYMER_STACK_CODEC).result().orElse(itemStack);
            } catch (Throwable ignored) {

            }
        }

        return itemStack;
    }

    /**
     * Returns stored identifier of Polymer ItemStack. If it's invalid, null is returned instead.
     */
    @Nullable
    public static Identifier getPolymerIdentifier(ItemStack itemStack) {
        var custom = itemStack.get(DataComponentTypes.CUSTOM_DATA);

        if (custom != null && custom.contains(POLYMER_STACK)) {
            try {
                return custom.get(POLYMER_STACK_ID_CODEC).result().orElse(null);
            } catch (Throwable ignored) {

            }
        }

        return null;
    }

    /**
     * Returns stored identifier of Polymer/other supported server mod ItemStack. If it's invalid, null is returned instead.
     */
    @Nullable
    public static Identifier getServerIdentifier(ItemStack itemStack) {
        /*if (itemStack.hasNbt()) {
            var id = getIdentifierFrom(itemStack.getNbt(), POLYMER_STACK);

            if (id == null) {
                id = getIdentifierFrom(itemStack.getNbt(), "PolyMcId");
            }

            return id;
        }*/

        return getPolymerIdentifier(itemStack);
    }

    @Nullable
    public static NbtCompound getPolymerNbt(ItemStack itemStack) {
        // todo
        //if (getPolymerIdentifier(itemStack) != null) {
        //    if (itemStack.getNbt().contains(REAL_TAG, NbtElement.COMPOUND_TYPE)) {
        //        return itemStack.getNbt().getCompound(REAL_TAG);
        //    }
        //}

        return null;
    }

    public static boolean isPolymerServerItem(ItemStack itemStack) {
        return isPolymerServerItem(itemStack, PolymerUtils.getPlayerContext());
    }

    public static boolean isPolymerServerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        if (getPolymerIdentifier(itemStack) != null) {
            return false;
        }
        if (itemStack.getItem() instanceof PolymerItem) {
            return true;
        }

        {
            var comp = itemStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS));

            if (comp != null && !comp.isEmpty()) {
                for (var ench : comp.getEnchantments()) {
                    if (ench.value() instanceof PolymerObject) {
                        if (ench.value() instanceof PolymerSyncedObject polymerEnchantment && polymerEnchantment.getPolymerReplacement(player) == ench.value()) {
                            continue;
                        }

                        return true;
                    }
                }
            }
        }
        {
            var comp = itemStack.get(DataComponentTypes.POTION_CONTENTS);

            if (comp != null) {
                if (comp.potion().isPresent() && comp.potion().get() instanceof PolymerObject) {
                    return true;
                }

                for (StatusEffectInstance statusEffectInstance : comp.customEffects()) {
                    if (statusEffectInstance.getEffectType() instanceof PolymerObject) {
                        return true;
                    }
                }
            }
        }
        {
            var comp = itemStack.get(DataComponentTypes.CONTAINER);
            if (comp != null) {
                for (var it = comp.iterator(); it.hasNext(); ) {
                    var nStack = it.next();
                    if (isPolymerServerItem(nStack, player)) {
                        return true;
                    }
                }
            }
        }
        if (CompatStatus.POLYMER_RESOURCE_PACK) {
            var display = itemStack.get(DataComponentTypes.DYED_COLOR);
            if (display != null) {
                return PolymerResourcePackUtils.isColorTaken(display.rgb());
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

        out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT.with(POLYMER_STACK_CODEC, itemStack).result().get());

        if (cmd != -1) {
            out.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(cmd));
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

        out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT.with(POLYMER_STACK_CODEC, itemStack).result().get());

        if (cmd == -1 && itemStack.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            out.set(DataComponentTypes.CUSTOM_MODEL_DATA, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
        } else if (cmd != -1) {
            out.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(cmd));
        }

        if (color == -1 && itemStack.contains(DataComponentTypes.DYED_COLOR)) {
            out.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(itemStack.get(DataComponentTypes.DYED_COLOR).rgb(), true));
        } else if (color != -1) {
            out.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, true));
        }

        if (itemStack.contains(DataComponentTypes.DAMAGE) && out.isDamageable()) {
            out.set(DataComponentTypes.DAMAGE, (int)
                    ((((double) itemStack.getDamage()) / itemStack.getItem().getMaxDamage()) * item.getMaxDamage()));
        }

        if (itemStack.contains(DataComponentTypes.ENCHANTMENTS)) {
            var enchantments = new Object2IntLinkedOpenHashMap<RegistryEntry<Enchantment>>();
            for (var e : itemStack.get(DataComponentTypes.ENCHANTMENTS).getEnchantmentsMap()) {
                if (e.getKey().value() instanceof PolymerSyncedObject polyEnch) {
                    var possible = (Enchantment) polyEnch.getPolymerReplacement(player);

                    if (possible != null) {
                        enchantments.put(Registries.ENCHANTMENT.getEntry(possible), e.getIntValue());
                    }
                } else if (!(e.getKey().value() instanceof PolymerObject)) {
                    enchantments.put(e.getKey(), e.getIntValue());
                }
            }

            if (!enchantments.isEmpty()) {
                out.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponentAccessor.create(enchantments, true));
            }
        }

        if (itemStack.contains(DataComponentTypes.POTION_CONTENTS)) {
            var comp = itemStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
            out.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.of(comp.getColor()), List.of()));
        }


        for (var i = 0; i < COMPONENTS_TO_COPY.length; i++) {
            var key = COMPONENTS_TO_COPY[i];
            out.set((DataComponentType) key, (Object) itemStack.get(key));
        }

        out.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, itemStack.hasGlint());
        out.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS));


        try {
            var tooltip = itemStack.getTooltip(player, tooltipContext);
            var name = (MutableText) tooltip.remove(0);

            if (!out.getName().equals(name)) {
                name.setStyle(name.getStyle().withParent(NON_ITALIC_STYLE));
                out.set(DataComponentTypes.CUSTOM_NAME, name);
            }

            if (itemStack.getItem() instanceof PolymerItem) {
                ((PolymerItem) itemStack.getItem()).modifyClientTooltip(tooltip, itemStack, player);
            }

            var lore = new ArrayList<Text>();
            for (Text t : tooltip) {
                lore.add(Text.empty().append(t).setStyle(PolymerItemUtils.CLEAN_STYLE));
            }
            out.set(DataComponentTypes.LORE, new LoreComponent(lore));
        } catch (Throwable e) {
            if (PolymerImpl.LOG_MORE_ERRORS) {
                PolymerImpl.LOGGER.error("Failed to get tooltip of " + itemStack, e);
            }

            // Fallback for mods that require client side methods for tooltips
            try {
                MutableText name = itemStack.getName().copy();

                if (!out.getName().equals(name)) {
                    name.setStyle(name.getStyle().withParent(NON_ITALIC_STYLE));
                    out.set(DataComponentTypes.CUSTOM_NAME, name);
                }
            } catch (Throwable e2) {
                // Fallback for mods that can't even handle names correctly...
                // Do nothing and hope for the best™

                if (PolymerImpl.LOG_MORE_ERRORS) {
                    PolymerImpl.LOGGER.error("Failed for second time. Ignoring.", e2);

                }
            }
        }

        return ITEM_MODIFICATION_EVENT.invoke((col) -> {
            var custom = out;

            for (var in : col) {
                custom = in.modifyItem(itemStack, custom, player);
            }

            return custom;
        });
    }

    private static NbtElement removeStackMarker(NbtElement nbt) {
        if (nbt instanceof NbtCompound compound) {
            var out = new NbtCompound();
            for (var entry : compound.getKeys()) {
                out.put(entry, removeStackMarker(compound.get(entry)));
            }
            return out;
        } else if (nbt instanceof NbtList list) {
            var out = new NbtList();
            for (var entry : list) {
                out.add(removeStackMarker(entry));
            }
            return out;
        }

        return nbt;
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

    public static ItemStack getClientItemStack(ItemStack stack, ServerPlayerEntity player) {
        var out = getPolymerItemStack(stack, player);
        if (CompatStatus.POLYMC) {
            out = PolyMcUtils.toVanilla(out, player);
        }
        return out;
    }

    @FunctionalInterface
    public interface ItemModificationEventHandler {
        ItemStack modifyItem(ItemStack original, ItemStack client, ServerPlayerEntity player);
    }

    public record ItemWithMetadata(Item item, int customModelData, int color) {
    }
}
