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
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.item.*;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
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

    public static final BooleanEvent<PolymerItemInteractionListener> POLYMER_ITEM_INTERACTION_CHECK = new BooleanEvent<>();

    private static final IdentityHashMap<Item, List<ComponentType<?>>> FORCE_SYNCED_COMPONENTS = new IdentityHashMap<>();


    private static final ComponentType<?>[] COMPONENTS_TO_COPY = {
            DataComponentTypes.CAN_BREAK,
            DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.BLOCK_ENTITY_DATA,
            DataComponentTypes.TRIM,
            DataComponentTypes.TOOL,
            DataComponentTypes.MAX_STACK_SIZE,
            DataComponentTypes.MAP_ID,
            DataComponentTypes.MAP_COLOR,
            DataComponentTypes.MAP_DECORATIONS,
            DataComponentTypes.MAP_POST_PROCESSING,
            DataComponentTypes.FOOD,
            DataComponentTypes.DAMAGE_RESISTANT,
            DataComponentTypes.FIREWORKS,
            DataComponentTypes.FIREWORK_EXPLOSION,
            DataComponentTypes.DAMAGE,
            DataComponentTypes.MAX_DAMAGE,
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.BANNER_PATTERNS,
            DataComponentTypes.BASE_COLOR,
            DataComponentTypes.CAN_BREAK,
            DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.REPAIR_COST,
            DataComponentTypes.BUNDLE_CONTENTS,
            DataComponentTypes.TOOLTIP_STYLE,
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
            DataComponentTypes.REPAIRABLE,
            DataComponentTypes.BLOCKS_ATTACKS,
            DataComponentTypes.BREAK_SOUND,
            DataComponentTypes.PROVIDES_BANNER_PATTERNS,
            DataComponentTypes.PROVIDES_TRIM_MATERIAL,
            DataComponentTypes.WEAPON,
            DataComponentTypes.TOOLTIP_DISPLAY
    };

    private static boolean stonecutterFix = PolymerImpl.FIX_STONECUTER;
    private static final ReferenceSet<ComponentType<?>> FORCE_HIDE_TOOLTIP = ReferenceSet.of(
            DataComponentTypes.UNBREAKABLE,
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.BLOCK_ENTITY_DATA,
            DataComponentTypes.CAN_BREAK,
            DataComponentTypes.CAN_PLACE_ON,
            DataComponentTypes.DAMAGE
    );

    private static final ReferenceSet<ComponentType<?>> IGNORE_TOOLTIP_HIDING = ReferenceSet.of(
        DataComponentTypes.LORE
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
     * @param context        Player being sent to
     * @return Client side ItemStack
     */
    public static ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipContext, PacketContext context) {
        if (getPolymerIdentifier(itemStack) != null) {
            return itemStack;
        } else if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, itemStack.getItem()) instanceof PolymerItem item) {
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
    public static void registerOverlay(Item item, PolymerItem polymerItem) {
        PolymerSyncedObject.setSyncedObject(Registries.ITEM, item, polymerItem);
        RegistrySyncUtils.setServerEntry(Registries.ITEM, item);
    }

    public static boolean isPolymerServerItem(ItemStack itemStack) {
        return isPolymerServerItem(itemStack, PacketContext.get());
    }

    public static boolean isPolymerServerItem(ItemStack itemStack, PacketContext context) {
        if (getPolymerIdentifier(itemStack) != null) {
            return false;
        }
        if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, itemStack.getItem()) instanceof PolymerItem) {
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

        if (itemStack.contains(DataComponentTypes.ENCHANTMENTS) && itemStack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT).shouldDisplay(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
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
     * @param context   Player seeing it
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
        if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, itemStack.getItem()) instanceof PolymerItem virtualItem) {
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

        if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
            polymerItem.modifyBasePolymerItemStack(out, itemStack, context);
        }

        var lookup = context.getRegistryWrapperLookup();

        {
            var current = itemStack.get(DataComponentTypes.USE_COOLDOWN);
            if (current == null) {
                out.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(0.00001f, Optional.of(Registries.ITEM.getId(itemStack.getItem()))));
            } else if (current.cooldownGroup().isEmpty()) {
                out.set(DataComponentTypes.USE_COOLDOWN, new UseCooldownComponent(current.seconds(), Optional.of(Registries.ITEM.getId(itemStack.getItem()))));
            }
        }


        out.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, itemStack.hasGlint());


        // Set item name
        {
            var name = itemStack.getItemName();
            out.set(DataComponentTypes.ITEM_NAME, name);

            if (!out.contains(DataComponentTypes.CUSTOM_NAME)) {
                if (
                        (item instanceof CompassItem && out.contains(DataComponentTypes.LODESTONE_TRACKER))
                                || (item instanceof PotionItem && out.contains(DataComponentTypes.POTION_CONTENTS))
                                || (item instanceof PlayerHeadItem && out.contains(DataComponentTypes.PROFILE) && Objects.requireNonNull(out.get(DataComponentTypes.PROFILE)).name().isPresent())

                ) {
                    out.set(DataComponentTypes.CUSTOM_NAME, Text.empty().append(name).setStyle(Style.EMPTY.withItalic(false)));
                }
            }
        }


        try {
            out.set(DataComponentTypes.CUSTOM_DATA, PolymerCommonUtils.executeWithoutNetworkingLogic(() -> {
                var comp = NbtComponent.of(
                        (NbtCompound) (storeCount ? POLYMER_STACK_CODEC : POLYMER_STACK_UNCOUNTED_CODEC).encoder()
                                .encodeStart(RegistryOps.of(NbtOps.INSTANCE, lookup), itemStack).getOrThrow()
                );
                if (storeCount) {
                    return comp.with(RegistryOps.of(NbtOps.INSTANCE, lookup), POLYMER_STACK_HAS_COUNT_CODEC, true).getOrThrow();
                } else {
                    return comp;
                }
            }));
        } catch (Throwable e) {
            out.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT.with(RegistryOps.of(NbtOps.INSTANCE, lookup), POLYMER_STACK_ID_CODEC, Registries.ITEM.getId(itemStack.getItem())).getOrThrow());
        }


        var display = out.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
        for (var x : out.getComponents()) {
            if (!IGNORE_TOOLTIP_HIDING.contains(x.type()) && (x.value() instanceof TooltipAppender || FORCE_HIDE_TOOLTIP.contains(x.type()))) {
                display = display.with(x.type(), true);
            }
        }
        out.set(DataComponentTypes.TOOLTIP_DISPLAY, display);

        try {
            var tooltip = itemStack.getTooltip(context.getPlayer() != null ? Item.TooltipContext.create(context.getPlayer().getRegistryManager()) : Item.TooltipContext.DEFAULT, context.getPlayer(), tooltipContext);
            if (!tooltip.isEmpty()) {
                tooltip.removeFirst();

                if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
                    polymerItem.modifyClientTooltip(tooltip, itemStack, context);
                }
                if (!tooltip.isEmpty()) {
                    var lore = new ArrayList<Text>();
                    for (Text t : tooltip) {
                        lore.add(Text.empty().append(t).setStyle(PolymerItemUtils.CLEAN_STYLE));
                    }
                    out.set(DataComponentTypes.LORE, new LoreComponent(lore));
                }
            } else {
                out.set(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(true, ReferenceSortedSets.emptySet()));
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
        while (PolymerSyncedObject.getSyncedObject(Registries.ITEM, out) instanceof PolymerItem newItem && newItem != item && req < maxDistance) {
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

    public static ItemStack getClientItemStack(ItemStack stack, PacketContext context) {
        var out = getPolymerItemStack(stack, context);
        if (CompatStatus.POLYMC) {
            out = PolyMcUtils.toVanilla(out, context.getPlayer());
        }
        return out;
    }

    public static boolean isPolymerItemInteraction(ServerPlayerEntity player, ItemStack stack, Hand hand, ServerWorld world, ActionResult actionResult) {
        if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isPolymerItemInteraction(player, hand, stack, world, actionResult)) {
            return true;
        }
        return POLYMER_ITEM_INTERACTION_CHECK.invoke((x) -> x.isPolymerItemInteraction(player, hand, stack, world, actionResult));
    }

    /**
     * This method allows to define Data Component Types, which need to be always synced to clients,
     * even if they have the default value for sent ItemStack.
     * This can be used with combination with Fabric's DefaultItemComponentEvents to synchronize modified components values to clients without the mod.
     *
     * @param item item this effect should apply to
     * @param types Component types that need to be always synced to client
     */
    public static void syncDefaultComponent(Item item, ComponentType<?>... types) {
        var list = FORCE_SYNCED_COMPONENTS.computeIfAbsent(item, (i) -> new ReferenceArrayList<>());
        for (var type : types) {
            if (!list.contains(type)) {
                list.add(type);
            }
        }
    }


    public static boolean isStonecutterFixEnabled() {
        return stonecutterFix;
    }

    public static void enableStonecutterFix() {
        stonecutterFix = true;
    }

    @UnmodifiableView
    public static List<ComponentType<?>> getSyncedDefaultComponents(Item item) {
        return FORCE_SYNCED_COMPONENTS.getOrDefault(item, List.of());
    }

    public static boolean isServerItem(ItemStack stack, PacketContext context) {
        if (isPolymerServerItem(stack, context)) {
            return true;
        }

        if (CompatStatus.POLYMC && PolyMcUtils.isServerSide(Registries.ITEM, stack.getItem())) {
            return true;
        }

        var container = stack.get(DataComponentTypes.CONTAINER);
        if (container != null) {
            for (var inner : container.iterateNonEmpty()) {
                if (isServerItem(inner, context)) {
                    return true;
                }
            }
        }

        var bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (var inner : bundle.iterate()) {
                if (isServerItem(inner, context)) {
                    return true;
                }
            }
        }

        var remainder = stack.get(DataComponentTypes.USE_REMAINDER);
        if (remainder != null) {
            if (isServerItem(remainder.convertInto(), context)) {
                return true;
            }
        }

        var projectile = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
        if (projectile != null) {
            for (var inner :projectile.getProjectiles()) {
                if (isServerItem(inner, context)) {
                    return true;
                }
            }
        }

        return false;
    }

    @FunctionalInterface
    public interface ItemModificationEventHandler {
        ItemStack modifyItem(ItemStack original, ItemStack client, PacketContext context);
    }

    @FunctionalInterface
    public interface PolymerItemInteractionListener {
        boolean isPolymerItemInteraction(ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, ActionResult actionResult);
    }

    public record ItemWithMetadata(Item item, @Nullable Identifier itemModel) {
    }
}
