package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public class CompatUtils {
    public static void iterateItems(Consumer<ItemStack> consumer) {
        for (var group : ItemGroups.getGroups()) {
            if (group.getType() != ItemGroup.Type.CATEGORY) {
                continue;
            }

            var stacks = ItemStackSet.create();

            stacks.addAll(((ClientItemGroupExtension) group).polymer$getStacksGroup());
            stacks.addAll(((ClientItemGroupExtension) group).polymer$getStacksSearch());

            if (stacks != null) {
                for (var stack : stacks) {
                    consumer.accept(stack);
                }
            }
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
}

