package eu.pb4.polymer.core.api.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.FunctionEvent;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.function.Predicate;

public final class PolymerItemUtils {
    public static final String POLYMER_STACK = "$polymer:stack";
    private static final String POLYMC_STACK = "PolyMcOriginal";
    public static final MapCodec<ItemStack> POLYMER_STACK_CODEC = ItemStack.CODEC.fieldOf(POLYMER_STACK);
    public static final MapCodec<ItemStack> POLYMER_STACK_UNCOUNTED_CODEC = ItemStack.UNCOUNTED_CODEC.fieldOf(POLYMER_STACK);
    public static final MapCodec<Boolean> POLYMER_STACK_HAS_COUNT_CODEC = Codec.BOOL.optionalFieldOf("$polymer:counted", false);
    public static final MapCodec<Identifier> POLYMER_STACK_ID_CODEC = Identifier.CODEC.fieldOf("id").fieldOf(POLYMER_STACK);

    private static final Codec<Map<Identifier, NbtElement>> COMPONENTS_CODEC = Codec.unboundedMap(Identifier.CODEC,
            Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
                var nbt = dynamic.convert(NbtOps.INSTANCE).getValue();
                return DataResult.success(nbt == dynamic.getValue() ? nbt.copy() : nbt);
            }, (nbt) -> new Dynamic<>(NbtOps.INSTANCE, nbt.copy())));

    public static final MapCodec<Map<Identifier, NbtElement>> POLYMER_STACK_COMPONENTS_CODEC = COMPONENTS_CODEC
            .optionalFieldOf("components", Map.of()).fieldOf(POLYMER_STACK);


    private static final MapCodec<ItemStack> POLYMC_STACK_CODEC = ItemStack.UNCOUNTED_CODEC.fieldOf(POLYMC_STACK);
    private static final MapCodec<Identifier> POLYMC_STACK_ID_CODEC = Identifier.CODEC.fieldOf("id").fieldOf(POLYMC_STACK);
    private static final MapCodec<Map<Identifier, NbtElement>> POLYMC_STACK_COMPONENTS_CODEC = COMPONENTS_CODEC.optionalFieldOf("components", Map.of()).fieldOf(POLYMC_STACK);

    public static final Style CLEAN_STYLE = Style.EMPTY.withItalic(false).withColor(Formatting.WHITE);
    /**
     * Allows to force rendering of some items as polymer one (for example vanilla ones)
     */
    public static final BooleanEvent<Predicate<ItemStack>> ITEM_CHECK = new BooleanEvent<>();
    /**
     * Allows to modify how virtual items looks before being sent to client (only if using build in methods!)
     * It can modify virtual version directly, as long as it's returned at the end.
     * You can also return new ItemStack, however please keep previous nbt so other modifications aren't removed if not needed!
     */
    public static final FunctionEvent<ItemModificationEventHandler, ItemStack> ITEM_MODIFICATION_EVENT = new FunctionEvent<>();
    private static final ComponentType<?>[] COMPONENTS_TO_COPY = {DataComponentTypes.CAN_BREAK, DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.BLOCK_ENTITY_DATA, DataComponentTypes.TRIM,
            DataComponentTypes.TOOL,
            DataComponentTypes.MAX_STACK_SIZE,
            DataComponentTypes.FOOD,
            DataComponentTypes.DAMAGE_RESISTANT,
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
            DataComponentTypes.JUKEBOX_PLAYABLE,
            DataComponentTypes.WRITABLE_BOOK_CONTENT,
            DataComponentTypes.WRITTEN_BOOK_CONTENT,
            DataComponentTypes.CONTAINER,
            DataComponentTypes.ENCHANTABLE,
            DataComponentTypes.USE_COOLDOWN,
            DataComponentTypes.CONSUMABLE,
            DataComponentTypes.EQUIPPABLE,
            DataComponentTypes.GLIDER,
            DataComponentTypes.CUSTOM_MODEL_DATA,
            DataComponentTypes.DYED_COLOR,
            DataComponentTypes.REPAIRABLE
    };
    @SuppressWarnings("rawtypes")
    private static final List<HideableTooltip> HIDEABLE_TOOLTIPS = List.of(
            HideableTooltip.of(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.TRIM, ArmorTrim::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.ofNeg(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent::isEmpty, ItemEnchantmentsComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.UNBREAKABLE, UnbreakableComponent::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_BREAK, BlockPredicatesChecker::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.CAN_PLACE_ON, BlockPredicatesChecker::withShowInTooltip),
            HideableTooltip.of(DataComponentTypes.JUKEBOX_PLAYABLE, JukeboxPlayableComponent::withShowInTooltip)
    );

    private PolymerItemUtils() {
    }

    /**
     * This method creates a client side ItemStack representation
     *
     * @param itemStack Server side ItemStack
     * @param context   Networking context
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, PacketContext context) {
        return getPolymerItemStack(itemStack, PolymerUtils.getTooltipType(context.getPlayer()), context);
    }

    /**
     * This method creates a client side ItemStack representation
     *
     * @param itemStack      Server side ItemStack
     * @param tooltipContext Tooltip Context
     * @param context         Player being sent to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipContext, PacketContext context) {
        if (getPolymerIdentifier(itemStack) != null) {
            return itemStack;
        } else if (itemStack.getItem() instanceof PolymerItem item) {
            return item.getPolymerItemStack(itemStack, tooltipContext, context);
        } else if (isPolymerServerItem(itemStack, context)) {
            return createItemStack(itemStack, tooltipContext, context);
        }

        if (ITEM_CHECK.invoke((x) -> x.test(itemStack))) {
            return createItemStack(itemStack, tooltipContext, context);
        }

        return itemStack;
    }

    /**
     * This method gets real ItemStack from Virtual/Client side one
     *
     * @param itemStack Client side ItemStack
     * @return Server side ItemStack
     */
    public static ItemStack getRealItemStack(ItemStack itemStack, RegistryWrapper.WrapperLookup lookup) {
        var custom = itemStack.get(DataComponentTypes.CUSTOM_DATA);

        if (custom != null && custom.contains(POLYMER_STACK)) {
            try {
                var counted = custom.get(POLYMER_STACK_HAS_COUNT_CODEC).result().orElse(Boolean.FALSE);

                //noinspection deprecation
                var x = (counted ? POLYMER_STACK_CODEC : POLYMER_STACK_UNCOUNTED_CODEC).decode(RegistryOps.of(NbtOps.INSTANCE, lookup), NbtOps.INSTANCE.getMap(custom.getNbt()).getOrThrow()).getOrThrow();

                if (!counted) {
                    x.setCount(itemStack.getCount());
                }

                return x;
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
        return getPolymerIdentifier(itemStack.get(DataComponentTypes.CUSTOM_DATA));
    }

    public static Identifier getPolymerIdentifier(@Nullable NbtComponent custom) {
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
        return getServerIdentifier(itemStack.get(DataComponentTypes.CUSTOM_DATA));
    }

    @Nullable
    public static Identifier getServerIdentifier(@Nullable NbtComponent nbtData) {
        if (nbtData == null) {
            return null;
        }
        var x = getPolymerIdentifier(nbtData);
        if (x != null) {
            return x;
        }

        if (nbtData.contains(POLYMC_STACK)) {
            try {
                return nbtData.get(POLYMC_STACK_ID_CODEC).result().orElse(null);
            } catch (Throwable ignored) {

            }
        }

        return null;
    }

    @Nullable
    public static Map<Identifier, NbtElement> getServerComponents(ItemStack stack) {
        return getServerComponents(stack.get(DataComponentTypes.CUSTOM_DATA));
    }

    @Nullable
    public static Map<Identifier, NbtElement> getPolymerComponents(ItemStack stack) {
        return getPolymerComponents(stack.get(DataComponentTypes.CUSTOM_DATA));
    }

    @Nullable
    public static Map<Identifier, NbtElement> getServerComponents(@Nullable NbtComponent nbtData) {
        if (nbtData == null) {
            return null;
        }
        var x = getPolymerComponents(nbtData);
        if (x != null) {
            return x;
        }

        if (nbtData.contains(POLYMC_STACK)) {
            try {
                return nbtData.get(POLYMC_STACK_COMPONENTS_CODEC).result().orElse(Map.of());
            } catch (Throwable ignored) {

            }
        }

        return null;
    }

    @Nullable
    public static Map<Identifier, NbtElement> getPolymerComponents(@Nullable NbtComponent nbtData) {
        if (nbtData == null || getPolymerIdentifier(nbtData) == null) {
            return null;
        }

        return nbtData.get(POLYMER_STACK_COMPONENTS_CODEC).result().orElse(Map.of());
    }

    public static boolean isPolymerServerItem(ItemStack itemStack) {
        return isPolymerServerItem(itemStack, PacketContext.get());
    }

    public static boolean isPolymerServerItem(ItemStack itemStack, PacketContext context) {
        if (getPolymerIdentifier(itemStack) != null) {
            return false;
        }
        if (itemStack.getItem() instanceof PolymerItem) {
            return true;
        }

        for (var x : itemStack.getComponentChanges().entrySet()) {
            if (!PolymerComponent.canSync(x.getKey(), x.getValue().orElse(null), context)) {
                return true;
            } else if (x.getValue() != null && x.getValue().isPresent()
                    && x.getValue().get() instanceof TransformingComponent t
                    && t.polymer$requireModification(context)) {
                return true;
            }
        }

        if (itemStack.contains(DataComponentTypes.ENCHANTMENTS) && itemStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).showInTooltip()) {
            for (var ench : itemStack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getEnchantments()) {
                var attributes = ench.value().getEffect(EnchantmentEffectComponentTypes.ATTRIBUTES);
                if (attributes != null) {
                    for (var attr : attributes) {
                        if (PolymerEntityUtils.isPolymerEntityAttribute(attr.attribute())
                                && DefaultAttributeRegistry.get(EntityType.PLAYER).has(attr.attribute())) {
                            return true;
                        }
                    }
                }
            }
        }

        return ITEM_CHECK.invoke((x) -> x.test(itemStack));
    }

    /**
     * This method creates full (vanilla like) representation of ItemStack
     *
     * @param itemStack Server side ItemStack
     * @param context    Player seeing it
     * @return Client side ItemStack
     */

    public static ItemStack createItemStack(ItemStack itemStack, PacketContext context) {
        return createItemStack(itemStack, PolymerUtils.getTooltipType(context.getPlayer()), context);
    }

    /**
     * This method creates full (vanilla like) representation of ItemStack
     *
     * @param itemStack      Server side ItemStack
     * @param tooltipContext TooltipContext
     * @param context        Player seeing it
     * @return Client side ItemStack
     */
    public static ItemStack createItemStack(ItemStack itemStack, TooltipType tooltipContext, PacketContext context) {
        Item item = itemStack.getItem();
        Identifier model = null;
        boolean storeCount;
        if (itemStack.getItem() instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, context);
            item = data.item();
            storeCount = virtualItem.shouldStorePolymerItemStackCount();
            model = data.itemModel != null ? data.itemModel : item.getComponents().get(DataComponentTypes.ITEM_MODEL);
        } else {
            storeCount = false;
            model = itemStack.get(DataComponentTypes.ITEM_MODEL);
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());
        for (var x : out.getComponents().getTypes()) {
            if (itemStack.getComponents().get(x) == null) {
                out.set(x, null);
            }
        }

        if (model != null) {
            out.set(DataComponentTypes.ITEM_MODEL, model);
        }

        for (var i = 0; i < COMPONENTS_TO_COPY.length; i++) {
            var key = COMPONENTS_TO_COPY[i];
            var x = itemStack.get(key);

            if (x instanceof TransformingComponent t) {
                //noinspection unchecked,rawtypes
                out.set((ComponentType) key, t.polymer$getTransformed(context));
            } else {
                //noinspection unchecked,rawtypes
                out.set((ComponentType) key, (Object) itemStack.get(key));
            }
        }
        var lookup = context.getRegistryWrapperLookup();

        try {
            PolymerCommonUtils.executeWithoutNetworkingLogic(() -> {
                var comp = NbtComponent.of(
                        (NbtCompound) (storeCount ? POLYMER_STACK_CODEC : POLYMER_STACK_UNCOUNTED_CODEC).encoder()
                                .encodeStart(RegistryOps.of(NbtOps.INSTANCE, lookup), itemStack).getOrThrow()
                );
                if (storeCount) {
                    out.set(DataComponentTypes.CUSTOM_DATA, comp.with(RegistryOps.of(NbtOps.INSTANCE, lookup), POLYMER_STACK_HAS_COUNT_CODEC, true).getOrThrow());
                } else {
                    out.set(DataComponentTypes.CUSTOM_DATA, comp);
                }
            });
        } catch (Throwable e) {
            out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT.with(RegistryOps.of(NbtOps.INSTANCE, lookup), POLYMER_STACK_ID_CODEC, Registries.ITEM.getId(itemStack.getItem())).getOrThrow());
        }

        if (!itemStack.contains(DataComponentTypes.USE_COOLDOWN)) {
            out.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.00001f, Optional.of(Registries.ITEM.getId(itemStack.getItem()))));
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
            var tooltip = itemStack.getTooltip(context.getPlayer() != null ? Item.TooltipContext.create(context.getPlayer().getWorld()) : Item.TooltipContext.DEFAULT, context.getPlayer(), tooltipContext);
            if (!tooltip.isEmpty()) {
                out.set(DataComponentTypes.ITEM_NAME, tooltip.remove(0));

                if (itemStack.getItem() instanceof PolymerItem) {
                    ((PolymerItem) itemStack.getItem()).modifyClientTooltip(tooltip, itemStack, context);
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
            out.set(DataComponentTypes.ITEM_NAME, itemStack.getOrDefault(DataComponentTypes.ITEM_NAME,
                    itemStack.getItem().getName(itemStack)));
        }
        return ITEM_MODIFICATION_EVENT.invoke((col) -> {
            var custom = out;

            for (var in : col) {
                custom = in.modifyItem(itemStack, custom, context);
            }

            return custom;
        });
    }

    /**
     * This method is minimal wrapper around {@link PolymerItem#getPolymerItem(ItemStack, PacketContext)} to make sure
     * It gets replaced if it represents other PolymerItem
     *
     * @param item        PolymerItem
     * @param stack       Server side ItemStack
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side ItemStack
     */
    public static ItemWithMetadata getItemSafely(PolymerItem item, ItemStack stack, PacketContext context, int maxDistance) {
        Item out = item.getPolymerItem(stack, context);
        PolymerItem lastVirtual = item;

        int req = 0;
        while (out instanceof PolymerItem newItem && newItem != item && req < maxDistance) {
            out = newItem.getPolymerItem(stack, context);
            lastVirtual = newItem;
            req++;
        }
        return new ItemWithMetadata(out, lastVirtual.getPolymerItemModel(stack, context));
    }

    /**
     * This method is minimal wrapper around {@link PolymerItem#getPolymerItem(ItemStack, PacketContext)} to make sure
     * It gets replaced if it represents other PolymerItem
     *
     * @param item  PolymerItem
     * @param stack Server side ItemStack
     * @return Client side ItemStack
     */
    public static ItemWithMetadata getItemSafely(PolymerItem item, ItemStack stack, PacketContext context) {
        return getItemSafely(item, stack, context, PolymerBlockUtils.NESTED_DEFAULT_DISTANCE);
    }

    /**
     * @deprecated Use {@link PolymerComponent#registerDataComponent(ComponentType[])} instead
     */
    @Deprecated
    public static void markAsPolymer(ComponentType<?>... types) {
        PolymerComponent.registerDataComponent(types);
    }

    /**
     * @deprecated Use {@link PolymerComponent#isPolymerComponent(ComponentType)} instead
     */
    @Deprecated
    public static boolean isPolymerComponent(ComponentType<?> type) {
        return PolymerComponent.isPolymerComponent(type);
    }

    public static ItemStack getClientItemStack(ItemStack stack, PacketContext context) {
        var out = getPolymerItemStack(stack, context);
        if (CompatStatus.POLYMC) {
            out = PolyMcUtils.toVanilla(out, context.getPlayer());
        }
        return out;
    }

    @FunctionalInterface
    public interface ItemModificationEventHandler {
        ItemStack modifyItem(ItemStack original, ItemStack client, PacketContext context);
    }

    public record ItemWithMetadata(Item item, Identifier itemModel) {
    }

    private record HideableTooltip<T>(ComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {

        public static <T> HideableTooltip<T> of(ComponentType<T> type, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, x -> true, setter);
        }

        public static <T> HideableTooltip<T> of(ComponentType<T> type, Predicate<T> shouldSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldSet, setter);
        }

        public static <T> HideableTooltip<T> ofNeg(ComponentType<T> type, Predicate<T> shouldntSet, TooltipSetter<T> setter) {
            return new HideableTooltip<>(type, shouldntSet.negate(), setter);
        }

        interface TooltipSetter<T> {
            T setTooltip(T val, boolean value);
        }
    }
}
