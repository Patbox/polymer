package eu.pb4.polymer.core.api.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.FunctionEvent;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.TransformingDataComponent;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.item.*;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
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
    private static final DataComponentType<?>[] COMPONENTS_TO_COPY = { DataComponentTypes.CAN_BREAK, DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.BLOCK_ENTITY_DATA, DataComponentTypes.TRIM,
            DataComponentTypes.TOOL,
            DataComponentTypes.MAX_STACK_SIZE,
            DataComponentTypes.FOOD,
            DataComponentTypes.FIRE_RESISTANT,
            DataComponentTypes.FIREWORKS,
            DataComponentTypes.FIREWORK_EXPLOSION,
            DataComponentTypes.DAMAGE,
            DataComponentTypes.MAX_DAMAGE,
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.BANNER_PATTERNS,
            DataComponentTypes.BASE_COLOR,
            DataComponentTypes.HIDE_TOOLTIP,
            DataComponentTypes.CAN_BREAK,
            DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.REPAIR_COST,
            DataComponentTypes.BUNDLE_CONTENTS,
            DataComponentTypes.RARITY,
            DataComponentTypes.LODESTONE_TRACKER,
            DataComponentTypes.ENCHANTMENTS,
            DataComponentTypes.STORED_ENCHANTMENTS,
            DataComponentTypes.POTION_CONTENTS,
            DataComponentTypes.CUSTOM_NAME,
    };

    @SuppressWarnings("rawtypes")
    private static final List<HideableTooltip> HIDEABLE_TOOLTIPS = List.of(
            HideableTooltip.of(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.TRIM, ArmorTrim::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.UNBREAKABLE, UnbreakableComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_BREAK, BlockPredicatesChecker::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_PLACE_ON, BlockPredicatesChecker::withShowInTooltip)
    );

    private static final Set<DataComponentType<?>> UNSYNCED_COMPONENTS = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);

    private PolymerItemUtils() {
    }

    /**
     * This method creates a client side ItemStack representation
     *
     * @param itemStack Server side ItemStack
     * @param player    Player being sent to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return getPolymerItemStack(itemStack, PolymerUtils.getTooltipType(player), player);
    }

    /**
     * This method creates a client side ItemStack representation
     *
     * @param itemStack      Server side ItemStack
     * @param tooltipContext Tooltip Context
     * @param player         Player being sent to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipContext, @Nullable ServerPlayerEntity player) {
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
        return getPolymerIdentifier(itemStack);
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

        for (var x : itemStack.getComponentChanges().entrySet()) {
            if (x.getValue() != null && x.getValue().isPresent()
                    && x.getValue().get() instanceof PolymerItemComponent c && c.canSyncRawToClient(player)) {
                return true;
            } else if (isPolymerComponent(x.getKey())) {
                return true;
            } else if (x.getValue() != null && x.getValue().isPresent()
                    && x.getValue().get() instanceof TransformingDataComponent t
                    && t.polymer$requireModification(player)) {
                return true;
            }
        }

        {
            var comp = itemStack.get(DataComponentTypes.CONTAINER);
            if (comp != null) {
                for (var nStack : comp.iterateNonEmpty()) {
                    if (isPolymerServerItem(nStack, player)) {
                        return true;
                    }
                }
            }
        }
        {
            var comp = itemStack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (comp != null) {
                for (var i = 0; i < comp.size(); i++) {
                    var nStack = comp.get(i);
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


        return ITEM_CHECK.invoke((x) -> x.test(itemStack));
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
        var x = itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        int cmd = x != null ? x.value() : -1;
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
        return createItemStack(itemStack, PolymerUtils.getTooltipType(player), player);
    }

    /**
     * This method creates full (vanilla like) representation of ItemStack
     *
     * @param itemStack      Server side ItemStack
     * @param tooltipContext TooltipContext
     * @param player         Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createItemStack(ItemStack itemStack, TooltipType tooltipContext, @Nullable ServerPlayerEntity player) {
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
        for (var x : out.getComponents().getTypes()) {
            if (itemStack.getComponents().get(x) == null) {
                out.set(x, null);
            }
        }

        out.set(DataComponentTypes.ITEM_NAME, itemStack.getOrDefault(DataComponentTypes.ITEM_NAME,
                itemStack.getItem().getName(itemStack)));

        for (var i = 0; i < COMPONENTS_TO_COPY.length; i++) {
            var key = COMPONENTS_TO_COPY[i];
            var x = itemStack.get(key);

            if (x instanceof TransformingDataComponent t) {
                //noinspection unchecked,rawtypes
                out.set((DataComponentType) key, t.polymer$getTransformed(player));
            } else {
                //noinspection unchecked,rawtypes
                out.set((DataComponentType) key, (Object) itemStack.get(key));
            }
        }

        out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT.with(POLYMER_STACK_CODEC, itemStack).result().get());

        if (cmd == -1 && itemStack.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            out.set(DataComponentTypes.CUSTOM_MODEL_DATA, itemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA));
        } else if (cmd != -1) {
            out.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(cmd));
        }

        if (color == -1 && itemStack.contains(DataComponentTypes.DYED_COLOR)) {
            out.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(getSafeColor(itemStack.get(DataComponentTypes.DYED_COLOR).rgb()), false));
        } else if (color != -1) {
            out.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, false));
        }

        out.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);

        for (var x : HIDEABLE_TOOLTIPS) {
            var a = out.get(x.type);
            //noinspection unchecked
            if (a != null && x.shouldSet.test(a)) {
                //noinspection unchecked
                out.set(x.type, x.setter.setTooltip(a, false));
            }
        }

        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, itemStack.hasGlint());

        try {
            var tooltip = itemStack.getTooltip(player != null ? Item.TooltipContext.create(player.getWorld()) : Item.TooltipContext.DEFAULT, player, tooltipContext);
            if (!tooltip.isEmpty()) {
                tooltip.remove(0);

                if (itemStack.getItem() instanceof PolymerItem) {
                    ((PolymerItem) itemStack.getItem()).modifyClientTooltip(tooltip, itemStack, player);
                }

                var lore = new ArrayList<Text>();
                for (Text t : tooltip) {
                    lore.add(Text.empty().append(t).setStyle(PolymerItemUtils.CLEAN_STYLE));
                }
                out.set(DataComponentTypes.LORE, new LoreComponent(lore));
            }
        } catch (Throwable e) {
            if (PolymerImpl.LOG_MORE_ERRORS) {
                PolymerImpl.LOGGER.error("Failed to get tooltip of " + itemStack, e);
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

    public static void markAsPolymer(DataComponentType<?>... types) {
        for (var x : types) {
            UNSYNCED_COMPONENTS.add(x);
            RegistrySyncUtils.setServerEntry(Registries.DATA_COMPONENT_TYPE, x);
        }
    }

    public static boolean isPolymerComponent(DataComponentType<?> type) {
        return UNSYNCED_COMPONENTS.add(type);
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

    private record HideableTooltip<T>(DataComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {

        public static <T> HideableTooltip<T> of(DataComponentType<T> type, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, x -> true, setter);
        }
        public static <T> HideableTooltip<T> of(DataComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldSet, setter);
        }

        public static <T> HideableTooltip<T> ofNeg(DataComponentType<T> type, Predicate<T> shouldntSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldntSet.negate(), setter);
        }

        interface TooltipSetter<T> {
            T setTooltip(T val, boolean value);
        }
    }
}
