package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CompatUtils {
    public static boolean areSamePolymerType(ItemStack a, ItemStack b) {
        return Objects.equals(getItemId(a.getItem(), a.get(DataComponentTypes.CUSTOM_DATA)), getItemId(b.getItem(), b.get(DataComponentTypes.CUSTOM_DATA)));
    }

    public static boolean areSamePolymerType(Item ai, NbtComponent a, Item bi, NbtComponent b) {
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
    public static Map<Identifier, NbtElement> getBackingComponents(ItemStack stack) {
        return PolymerItemUtils.getServerComponents(stack);
    }

    public static boolean isServerSide(ItemStack stack) {
        return PolymerItemUtils.getServerIdentifier(stack) != null;
    }

    public static boolean isServerSide(@Nullable NbtComponent component) {
        return PolymerItemUtils.getServerIdentifier(component) != null;
    }

    @Nullable
    public static Object getKey(ItemStack stack) {
        return getKey(stack.get(DataComponentTypes.CUSTOM_DATA));
    }
    public static Object getKey(@Nullable NbtComponent component) {
        var id = PolymerItemUtils.getServerIdentifier(component);
        if (id == null) {
            return null;
        }

        if (InternalClientRegistry.ITEMS.contains(id)) {
            return InternalClientRegistry.ITEMS.getKey(id);
        }

        return Registries.ITEM.get(id);
    }

    private static Identifier getItemId(Item item, @Nullable NbtComponent nbtComponent) {
        var id = PolymerItemUtils.getServerIdentifier(nbtComponent);

        if (id == null) {
            return item.getRegistryEntry().registryKey().getValue();
        }

        return id;
    }


    public static void iterateItems(Consumer<ItemStack> consumer) {
        var stacks = ItemStackSet.create();

        for (var group : ItemGroups.getGroups()) {
            if (group.getType() != ItemGroup.Type.CATEGORY) {
                continue;
            }
            stacks.addAll(((ClientItemGroupExtension) group).polymer$getStacksGroup());
            stacks.addAll(((ClientItemGroupExtension) group).polymer$getStacksSearch());
        }

        for (var stack : stacks) {
            consumer.accept(stack);
        }
    }

    public static String getModName(ItemStack stack) {
        var id = PolymerItemUtils.getServerIdentifier(stack);
        if (id != null) {
            return InternalClientRegistry.getModName(id);
        }
        return null;
    }

    public static Identifier getId(@Nullable NbtComponent nbt) {
        return PolymerItemUtils.getServerIdentifier(nbt);
    }

    public record Key(Identifier identifier) {}
}

