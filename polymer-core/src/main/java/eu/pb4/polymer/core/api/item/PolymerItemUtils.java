package eu.pb4.polymer.core.api.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.FunctionEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.other.PacketTooltipContext;
import eu.pb4.polymer.core.mixin.CustomDataAccessor;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * General utility methods used for handling polymer items
 */
public final class PolymerItemUtils {
    public static final String POLYMER_STACK = "$polymer:stack";
    public static final String POLYMER_COUNTED = "$polymer:counted";
    private static final String POLYMC_STACK = "PolyMcOriginal";
    private static final Codec<Identifier> STACK_ID_CODEC = Identifier.CODEC.fieldOf("id").codec();

    private static final Codec<Map<Identifier, Tag>> COMPONENTS_CODEC = Codec.unboundedMap(Identifier.CODEC,
            Codec.PASSTHROUGH.comapFlatMap((dynamic) -> {
                var nbt = dynamic.convert(NbtOps.INSTANCE).getValue();
                return DataResult.success(nbt == dynamic.getValue() ? nbt.copy() : nbt);
            }, (nbt) -> new Dynamic<>(NbtOps.INSTANCE, nbt.copy()))).optionalFieldOf("components", Map.of()).codec();
    public static final Style CLEAN_STYLE = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);
    /**
     * Allows to force rendering of some items as polymer one (for example vanilla ones)
     */
    public static final BooleanEvent<BiPredicate<ItemStack, PacketContext>> CONTEXT_ITEM_CHECK = new BooleanEvent<>();

    @Deprecated(forRemoval = true)
    public static final BooleanEvent<Predicate<ItemStack>> ITEM_CHECK = new BooleanEvent<>();
    /**
     * Allows to modify how virtual items looks before being sent to client (only if using build in methods!)
     * It can modify virtual version directly, as long as it's returned at the end.
     * You can also return new ItemStack, however please keep previous nbt so other modifications aren't removed if not needed!
     */
    public static final FunctionEvent<ItemModificationEventHandler, ItemStack> ITEM_MODIFICATION_EVENT = new FunctionEvent<>();

    /**
     * Allows to run additional logic, making interactions work correctly server side,
     * emulating or preventing otherwise client dictated behaviour.
     *
     * See {@link PolymerItem#isPolymerItemInteraction(ServerPlayer, InteractionHand, ItemStack, ServerLevel, InteractionResult)}
     */
    public static final BooleanEvent<PolymerItemInteractionListener> POLYMER_ITEM_INTERACTION_CHECK = new BooleanEvent<>();
    /**
     * Changes sound logic within the item use interaction code to always play sounds to the client.
     *
     * See {@link PolymerItem#isIgnoringItemInteractionPlaySoundExceptedEntity(ServerPlayer, InteractionHand, ItemStack, ServerLevel)}
     */
    public static final BooleanEvent<PolymerIgnoreSoundExceptionListener> POLYMER_IGNORE_SOUND_EXCEPTED_ENTITY = new BooleanEvent<>();
    /**
     * Event for extending which items should be considered to be server items (have different data on the client).
     */
    public static final BooleanEvent<ServerItemPredicate> IS_SERVER_ITEM_EVENT = new BooleanEvent<>();

    private static final IdentityHashMap<Item, List<DataComponentType<?>>> FORCE_SYNCED_COMPONENTS = new IdentityHashMap<>();


    private static final DataComponentType<?>[] COMPONENTS_TO_COPY = {
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON,
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.TRIM,
            DataComponents.TOOL,
            DataComponents.MAX_STACK_SIZE,
            DataComponents.MAP_ID,
            DataComponents.MAP_COLOR,
            DataComponents.MAP_DECORATIONS,
            DataComponents.MAP_POST_PROCESSING,
            DataComponents.FOOD,
            DataComponents.DAMAGE_RESISTANT,
            DataComponents.FIREWORKS,
            DataComponents.FIREWORK_EXPLOSION,
            DataComponents.DAMAGE,
            DataComponents.MAX_DAMAGE,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.BANNER_PATTERNS,
            DataComponents.BASE_COLOR,
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON,
            DataComponents.REPAIR_COST,
            DataComponents.BUNDLE_CONTENTS,
            DataComponents.TOOLTIP_STYLE,
            DataComponents.RARITY,
            DataComponents.LODESTONE_TRACKER,
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS,
            DataComponents.POTION_CONTENTS,
            DataComponents.CUSTOM_NAME,
            DataComponents.JUKEBOX_PLAYABLE,
            DataComponents.WRITABLE_BOOK_CONTENT,
            DataComponents.WRITTEN_BOOK_CONTENT,
            DataComponents.CONTAINER,
            DataComponents.ENCHANTABLE,
            DataComponents.USE_COOLDOWN,
            DataComponents.CONSUMABLE,
            DataComponents.EQUIPPABLE,
            DataComponents.GLIDER,
            DataComponents.CUSTOM_MODEL_DATA,
            DataComponents.DYED_COLOR,
            DataComponents.REPAIRABLE,
            DataComponents.BLOCKS_ATTACKS,
            DataComponents.BREAK_SOUND,
            DataComponents.PROVIDES_BANNER_PATTERNS,
            DataComponents.PROVIDES_TRIM_MATERIAL,
            DataComponents.CHARGED_PROJECTILES,
            DataComponents.WEAPON,
            DataComponents.TOOLTIP_DISPLAY,
            DataComponents.KINETIC_WEAPON,
            DataComponents.PIERCING_WEAPON,
            DataComponents.ATTACK_RANGE,
            DataComponents.MINIMUM_ATTACK_CHARGE,
            DataComponents.SWING_ANIMATION,
            DataComponents.USE_EFFECTS
    };

    private static boolean stonecutterFix = PolymerImpl.FIX_STONECUTER;
    private static final ReferenceSet<DataComponentType<?>> FORCE_HIDE_TOOLTIP = ReferenceSet.of(
            DataComponents.UNBREAKABLE,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.BLOCK_ENTITY_DATA,
            DataComponents.CAN_BREAK,
            DataComponents.CAN_PLACE_ON
    );

    private static final ReferenceSet<DataComponentType<?>> IGNORE_TOOLTIP_HIDING = ReferenceSet.of(
        DataComponents.LORE
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
    public static ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipContext, PacketContext context) {
        if (getPolymerIdentifier(itemStack) != null) {
            return itemStack;
        } else if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem item) {
            return item.getPolymerItemStack(itemStack, tooltipContext, context);
        } else if (isPolymerServerItem(itemStack, context)) {
            return createItemStack(itemStack, tooltipContext, context);
        }

        if (CONTEXT_ITEM_CHECK.invoke((x) -> x.test(itemStack, context))) {
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
    public static ItemStack getRealItemStack(ItemStack itemStack, HolderLookup.Provider lookup) {
        var custom = itemStack.get(DataComponents.CUSTOM_DATA);


        if (custom != null) {
            var val = ((CustomDataAccessor) (Object) custom).polymer$getNbtUnsafe();

            if (!val.contains(POLYMER_STACK)) {
                return itemStack;
            }

            try {
                var counted = val.getBooleanOr(POLYMER_COUNTED, false);

                var x = val.read(POLYMER_STACK, (counted ? ItemStack.CODEC : ItemStack.SINGLE_ITEM_CODEC), lookup.createSerializationContext(NbtOps.INSTANCE)).orElseGet(itemStack::copy);

                if (!counted) {
                    x.setCount(itemStack.getCount());
                }

                return x;
            } catch (Throwable e) {
                if (PolymerImpl.LOG_MORE_ERRORS) {
                    PolymerImpl.LOGGER.warn("Failed to decode Item Stack!", e);
                }
            }
        }

        return itemStack;
    }

    /**
     * Returns stored identifier of Polymer ItemStack. If it's invalid, null is returned instead.
     */
    @Nullable
    public static Identifier getPolymerIdentifier(ItemStack itemStack) {
        return getPolymerIdentifier(itemStack.get(DataComponents.CUSTOM_DATA));
    }

    public static Identifier getPolymerIdentifier(@Nullable CustomData custom) {
        if (custom != null) {
            var val = ((CustomDataAccessor) (Object) custom).polymer$getNbtUnsafe();
            if (!val.contains(POLYMER_STACK)) {
                return null;
            }
            try {
                return val.read(POLYMER_STACK, STACK_ID_CODEC).orElse(null);
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
        return getServerIdentifier(itemStack.get(DataComponents.CUSTOM_DATA));
    }

    @Nullable
    public static Identifier getServerIdentifier(@Nullable CustomData nbtData) {
        if (nbtData == null) {
            return null;
        }
        var x = getPolymerIdentifier(nbtData);
        if (x != null) {
            return x;
        }

        try {
            //noinspection DataFlowIssue
            var nbt = ((CustomDataAccessor) (Object) nbtData).polymer$getNbtUnsafe();
            if (nbt.contains(POLYMC_STACK)) {
                return nbt.read(POLYMC_STACK, STACK_ID_CODEC).orElse(null);
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    @Nullable
    public static Map<Identifier, Tag> getServerComponents(ItemStack stack) {
        return getServerComponents(stack.get(DataComponents.CUSTOM_DATA));
    }

    @Nullable
    public static Map<Identifier, Tag> getPolymerComponents(ItemStack stack) {
        return getPolymerComponents(stack.get(DataComponents.CUSTOM_DATA));
    }

    @Nullable
    public static Map<Identifier, Tag> getServerComponents(@Nullable CustomData nbtData) {
        if (nbtData == null) {
            return null;
        }
        var x = getPolymerComponents(nbtData);
        if (x != null) {
            return x;
        }


        var nbt = ((CustomDataAccessor) (Object) nbtData).polymer$getNbtUnsafe();
        if (nbt.contains(POLYMC_STACK)) {
            return nbt.read(POLYMC_STACK, COMPONENTS_CODEC).orElse(Map.of());
        }

        return null;
    }

    @Nullable
    public static Map<Identifier, Tag> getPolymerComponents(@Nullable CustomData nbtData) {
        if (nbtData == null || getPolymerIdentifier(nbtData) == null) {
            return null;
        }
        var nbt = ((CustomDataAccessor) (Object) nbtData).polymer$getNbtUnsafe();
        if (!nbt.contains(POLYMER_STACK)) {
            return null;
        }

        return nbt.read(POLYMER_STACK, COMPONENTS_CODEC).orElse(Map.of());
    }
    public static void registerOverlay(Item item, PolymerItem polymerItem) {
        PolymerItem.registerOverlay(item, polymerItem);
    }

    public static boolean isPolymerServerItem(ItemStack itemStack) {
        return isPolymerServerItem(itemStack, PacketContext.get());
    }

    public static boolean isPolymerServerItem(ItemStack itemStack, PacketContext context) {
        if (getPolymerIdentifier(itemStack) != null) {
            return false;
        }
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem) {
            return true;
        }

        for (var x : itemStack.getComponentsPatch().entrySet()) {
            if (!PolymerComponent.canSync(x.getKey(), x.getValue().orElse(null), context)) {
                return true;
            } else if (x.getValue() != null && x.getValue().isPresent()
                    && x.getValue().get() instanceof TransformingComponent t
                    && t.polymer$requireModification(context)) {
                return true;
            }
        }

        if (itemStack.has(DataComponents.ENCHANTMENTS) && itemStack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT).shows(DataComponents.ATTRIBUTE_MODIFIERS)) {
            for (var ench : itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).keySet()) {
                var attributes = ench.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES);
                if (attributes != null) {
                    for (var attr : attributes) {
                        if (PolymerEntityUtils.isPolymerEntityAttribute(attr.attribute())
                                && DefaultAttributes.getSupplier(EntityType.PLAYER).hasAttribute(attr.attribute())) {
                            return true;
                        }
                    }
                }
            }
        }

        return CONTEXT_ITEM_CHECK.invoke((x) -> x.test(itemStack, context));
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
    public static ItemStack createItemStack(ItemStack itemStack, TooltipFlag tooltipContext, PacketContext context) {
        Item item = itemStack.getItem();
        Identifier model = null;
        boolean storeCount;
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem virtualItem) {
            var data = PolymerItemUtils.getItemSafely(virtualItem, itemStack, context);
            item = data.item();
            storeCount = virtualItem.shouldStorePolymerItemStackCount();
            model = data.itemModel != null ? data.itemModel : item.components().get(DataComponents.ITEM_MODEL);
        } else {
            storeCount = false;
            model = itemStack.get(DataComponents.ITEM_MODEL);
        }

        ItemStack out = new ItemStack(item, itemStack.getCount());
        for (var x : out.getComponents().keySet()) {
            if (itemStack.getComponents().get(x) == null) {
                out.set(x, null);
            }
        }

        if (model != null) {
            out.set(DataComponents.ITEM_MODEL, model);
        }

        for (var i = 0; i < COMPONENTS_TO_COPY.length; i++) {
            var key = COMPONENTS_TO_COPY[i];
            var x = itemStack.get(key);

            if (x instanceof TransformingComponent t) {
                //noinspection unchecked,rawtypes
                out.set((DataComponentType) key, t.polymer$getTransformed(context));
            } else {
                //noinspection unchecked,rawtypes
                out.set((DataComponentType) key, (Object) itemStack.get(key));
            }
        }

        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
            polymerItem.modifyBasePolymerItemStack(out, itemStack, context);
        }

        var lookup = context.getRegistryWrapperLookup();

        {
            var current = itemStack.get(DataComponents.USE_COOLDOWN);
            if (current == null) {
                out.set(DataComponents.USE_COOLDOWN, new UseCooldown(0.00001f, Optional.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))));
            } else if (current.cooldownGroup().isEmpty()) {
                out.set(DataComponents.USE_COOLDOWN, new UseCooldown(current.seconds(), Optional.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))));
            }
        }


        out.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, itemStack.hasFoil());


        // Set item name
        {
            var name = itemStack.getItemName();
            out.set(DataComponents.ITEM_NAME, name);

            if (!out.has(DataComponents.CUSTOM_NAME)) {
                if (
                        (item instanceof CompassItem && out.has(DataComponents.LODESTONE_TRACKER))
                                || ((item instanceof PotionItem || item instanceof TippedArrowItem) && out.has(DataComponents.POTION_CONTENTS))
                                || (item instanceof PlayerHeadItem && out.has(DataComponents.PROFILE) && Objects.requireNonNull(out.get(DataComponents.PROFILE)).name().isPresent())

                ) {
                    out.set(DataComponents.CUSTOM_NAME, Component.empty().append(name).setStyle(Style.EMPTY.withItalic(false)));
                }
            }
        }


        try {
            out.set(DataComponents.CUSTOM_DATA, PolymerCommonUtils.executeWithoutNetworkingLogic(() -> {
                var nbt = new CompoundTag();

                nbt.store(POLYMER_STACK, storeCount ? ItemStack.CODEC : ItemStack.SINGLE_ITEM_CODEC, lookup.createSerializationContext(NbtOps.INSTANCE), itemStack);

                if (storeCount) {
                    nbt.putBoolean(POLYMER_COUNTED, true);
                }

                return CustomData.of(nbt);
            }));
        } catch (Throwable e) {
            var profile = context.getGameProfile();
            CommonImpl.LOGGER.error("Failed to encode Polymer item stack data {} for {}", itemStack, profile != null ? profile.name() : "<Unknown>");
        }


        var display = out.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);

        for (var x : out.getComponents()) {
            if (!IGNORE_TOOLTIP_HIDING.contains(x.type()) && (x.value() instanceof TooltipProvider || FORCE_HIDE_TOOLTIP.contains(x.type()))) {
                display = display.withHidden(x.type(), true);
            }
        }
        if (out.has(DataComponents.DAMAGE) && !itemStack.has(DataComponents.DAMAGE)) {
            display = display.withHidden(DataComponents.DAMAGE, true);
        }

        display.hiddenComponents().removeIf(PolymerComponent::isPolymerComponent);
        out.set(DataComponents.TOOLTIP_DISPLAY, display);

        try {
            var tooltip = itemStack.getTooltipLines(new PacketTooltipContext(context), context.getPlayer(), tooltipContext);
            if (!tooltip.isEmpty()) {
                tooltip.removeFirst();

                if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
                    polymerItem.modifyClientTooltip(tooltip, itemStack, context);
                }
                if (!tooltip.isEmpty()) {
                    var lore = new ArrayList<Component>();
                    for (Component t : tooltip) {
                        lore.add(Component.empty().append(t).setStyle(PolymerItemUtils.CLEAN_STYLE));
                    }
                    out.set(DataComponents.LORE, new ItemLore(lore));
                }
            } else {
                out.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
            }
        } catch (Throwable e) {
            if (PolymerImpl.LOG_MORE_ERRORS) {
                PolymerImpl.LOGGER.error("Failed to get tooltip of " + itemStack, e);
            }
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
        while (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, out) instanceof PolymerItem newItem && newItem != item && req < maxDistance) {
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

    public static boolean isPolymerItemInteraction(ServerPlayer player, ItemStack stack, InteractionHand hand, ServerLevel world, InteractionResult actionResult) {
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isPolymerItemInteraction(player, hand, stack, world, actionResult)) {
            return true;
        }
        return POLYMER_ITEM_INTERACTION_CHECK.invoke((x) -> x.isPolymerItemInteraction(player, hand, stack, world, actionResult));
    }

    public static boolean isIgnoringPlaySoundExceptedEntity(ServerPlayer player, ItemStack stack, InteractionHand hand, ServerLevel world) {
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isIgnoringItemInteractionPlaySoundExceptedEntity(player, hand, stack, world)) {
            return true;
        }
        return POLYMER_IGNORE_SOUND_EXCEPTED_ENTITY.invoke((x) -> x.isIgnoringItemInteractionPlaySoundExceptedEntity(player, hand, stack, world));
    }

    /**
     * This method allows to define Data Component Types, which need to be always synced to clients,
     * even if they have the default value for sent ItemStack.
     * This can be used with combination with Fabric's DefaultItemComponentEvents to synchronize modified components values to clients without the mod.
     *
     * @param item item this effect should apply to
     * @param types Component types that need to be always synced to client
     */
    public static void syncDefaultComponent(Item item, DataComponentType<?>... types) {
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
    public static List<DataComponentType<?>> getSyncedDefaultComponents(Item item) {
        return FORCE_SYNCED_COMPONENTS.getOrDefault(item, List.of());
    }

    public static boolean isServerItem(ItemStack stack, PacketContext context) {
        if (isPolymerServerItem(stack, context)) {
            return true;
        }

        if (CompatStatus.POLYMC && PolyMcUtils.isServerSide(BuiltInRegistries.ITEM, stack.getItem())) {
            return true;
        }

        var container = stack.get(DataComponents.CONTAINER);
        if (container != null) {
            for (var inner : container.nonEmptyItems()) {
                if (isServerItem(inner, context)) {
                    return true;
                }
            }
        }

        var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            for (var inner : bundle.items()) {
                if (isServerItem(inner, context)) {
                    return true;
                }
            }
        }

        var remainder = stack.get(DataComponents.USE_REMAINDER);
        if (remainder != null) {
            if (isServerItem(remainder.convertInto(), context)) {
                return true;
            }
        }

        var projectile = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (projectile != null) {
            for (var inner : projectile.getItems()) {
                if (isServerItem(inner, context)) {
                    return true;
                }
            }
        }

        return IS_SERVER_ITEM_EVENT.invoke(x -> x.isServerItem(stack, context));
    }

    @FunctionalInterface
    public interface ItemModificationEventHandler {
        ItemStack modifyItem(ItemStack original, ItemStack client, PacketContext context);
    }

    @FunctionalInterface
    public interface PolymerItemInteractionListener {
        boolean isPolymerItemInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, InteractionResult actionResult);
    }

    @FunctionalInterface
    public interface PolymerIgnoreSoundExceptionListener {
        boolean isIgnoringItemInteractionPlaySoundExceptedEntity(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world);
    }

    @FunctionalInterface
    public interface ServerItemPredicate {
        boolean isServerItem(ItemStack stack, PacketContext context);
    }

    public record ItemWithMetadata(Item item, @Nullable Identifier itemModel) {
    }

    static {
        CONTEXT_ITEM_CHECK.register((stack, context) -> ITEM_CHECK.invoke(x -> x.test(stack)));
    }
}
