package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientCreativeModeTabExtension;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CompatUtils {
    public static boolean areSamePolymerType(ItemStack a, ItemStack b) {
        return Objects.equals(getItemId(a.getItem(), a.get(DataComponents.CUSTOM_DATA)), getItemId(b.getItem(), b.get(DataComponents.CUSTOM_DATA)));
    }

    public static boolean areSamePolymerType(Object ai, CustomData a, Object bi, CustomData b) {
        return Objects.equals(getItemId(ai, a), getItemId(bi, b));
    }

    public static boolean areEqualItems(ItemStack a, ItemStack b) {
        if (!areSamePolymerType(a, b)) {
            return false;
        }
        var nbtA = getBackingComponents(a);
        var nbtB = getBackingComponents(b);
        return Objects.equals(nbtA, nbtB);
    }

    @Nullable
    public static Map<Identifier, Tag> getBackingComponents(ItemStack stack) {
        return PolymerItemUtils.getPolymerComponents(stack);
    }

    public static boolean isServerSide(ItemStack stack) {
        return PolymerItemUtils.getPolymerIdentifier(stack) != null;
    }

    public static boolean isServerSide(@Nullable CustomData component) {
        return PolymerItemUtils.getPolymerIdentifier(component) != null;
    }

    @Nullable
    public static Object getKey(ItemStack stack) {
        return getKey(stack.get(DataComponents.CUSTOM_DATA));
    }
    public static Object getKey(@Nullable CustomData component) {
        var id = PolymerItemUtils.getPolymerIdentifier(component);
        if (id == null) {
            return null;
        }

        if (InternalClientRegistry.ITEMS.contains(id)) {
            return InternalClientRegistry.ITEMS.getKey(id);
        }

        return BuiltInRegistries.ITEM.getValue(id);
    }

    private static Identifier getItemId(Object item, @Nullable CustomData nbtComponent) {
        var id = PolymerItemUtils.getPolymerIdentifier(nbtComponent);

        if (id == null && item instanceof Item item1) {
            return item1.builtInRegistryHolder().key().identifier();
        }

        return id;
    }


    public static void iterateItems(Consumer<ItemStack> consumer) {
        var stacks = ItemStackLinkedSet.createTypeAndComponentsSet();

        for (var group : CreativeModeTabs.allTabs()) {
            if (group.getType() != CreativeModeTab.Type.CATEGORY) {
                continue;
            }
            stacks.addAll(((ClientCreativeModeTabExtension) group).polymer$getStacksGroup());
            stacks.addAll(((ClientCreativeModeTabExtension) group).polymer$getStacksSearch());
        }

        for (var stack : stacks) {
            consumer.accept(stack);
        }
    }

    public static Identifier getId(@Nullable CustomData nbt) {
        return PolymerItemUtils.getPolymerIdentifier(nbt);
    }
}

