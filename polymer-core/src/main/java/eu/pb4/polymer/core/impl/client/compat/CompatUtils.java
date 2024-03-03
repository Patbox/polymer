package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CompatUtils {
    public static boolean areSamePolymerType(ItemStack a, ItemStack b) {
        return Objects.equals(getItemId(a), getItemId(b));
    }

    public static boolean areEqualItems(ItemStack a, ItemStack b) {
        if (!areSamePolymerType(a, b)) {
            return false;
        }
        var nbtA = getBackingNbt(a);
        var nbtB = getBackingNbt(b);
        return Objects.equals(nbtA, nbtB);
    }

    @Nullable
    public static NbtCompound getBackingNbt(ItemStack stack) {
        /*if (!stack.hasNbt()) {
            return null;
        }
        var nbt = stack.getNbt();
        if (PolymerItemUtils.getServerIdentifier(stack) == null) {
            return nbt;
        }

        var maybeNbt = PolymerItemUtils.getPolymerNbt(stack);

        if (maybeNbt != null) {
            return maybeNbt;
        }
        maybeNbt = nbt.getCompound("PolyMcOriginal");

        return maybeNbt != null && maybeNbt.contains("tag", NbtElement.COMPOUND_TYPE) ? maybeNbt.getCompound("tag") : null;*/
        return null;
    }

    public static boolean isServerSide(ItemStack stack) {
        return PolymerItemUtils.getServerIdentifier(stack) != null;
    }

    public static Object getKey(ItemStack stack) {
        var id = PolymerItemUtils.getServerIdentifier(stack);
        if (id == null) {
            return stack.getItem();
        }

        if (InternalClientRegistry.ITEMS.contains(id)) {
            return InternalClientRegistry.ITEMS.getKey(id);
        }

        return Registries.ITEM.get(id);
    }

    private static Identifier getItemId(ItemStack stack) {
        var id = PolymerItemUtils.getServerIdentifier(stack);

        if (id == null) {
            return stack.getItem().getRegistryEntry().registryKey().getValue();
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

    public record Key(Identifier identifier) {}
}

