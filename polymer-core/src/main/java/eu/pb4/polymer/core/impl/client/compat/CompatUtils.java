package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CompatUtils {
    public static boolean areSamePolymerType(ItemStack a, ItemStack b) {
        return Objects.equals(getItemId(a), getItemId(b));
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

    public static void registerReload(Runnable runnable) {
        if (PolymerImpl.IS_CLIENT) {
            PolymerClientUtils.ON_CLEAR.register(() -> {
                if (MinecraftClient.getInstance().world != null) {
                    runnable.run();
                }
            });
            PolymerClientUtils.ON_SEARCH_REBUILD.register(() -> {
                if (MinecraftClient.getInstance().world != null) {
                    runnable.run();
                }
            });
        }
    }

    public static String getModName(ItemStack stack) {
        var id = PolymerItemUtils.getServerIdentifier(stack);
        if (id != null) {
            return InternalClientRegistry.getModName(id);
        }
        return null;
    }
}

