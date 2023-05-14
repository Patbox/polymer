package eu.pb4.polymer.impl.client.compat;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public class JeiCompatibility implements IModPlugin {
    private static final Identifier ID = new Identifier("polymer", "jei_plugin");

    private static final Predicate<ItemStack> SHOULD_REMOVE = (stack) -> (PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getServerIdentifier(stack) != null);

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (PolymerImpl.IS_CLIENT) {
            update(registration.getIngredientManager());
        }
    }

    public static boolean areSamePolymerItem(ItemStack a, ItemStack b) {
        return Objects.equals(PolymerItemUtils.getServerIdentifier(a), PolymerItemUtils.getServerIdentifier(a));
    }

    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (PolymerImpl.IS_CLIENT) {
            PolymerClientUtils.ON_CLEAR.register(() -> update(jeiRuntime.getIngredientManager()));
            PolymerClientUtils.ON_SEARCH_REBUILD.register(() -> update(jeiRuntime.getIngredientManager()));
        }
    }

    private static void update(IIngredientManager manager) {
        if (!MinecraftClient.getInstance().isOnThread()) {
            MinecraftClient.getInstance().execute(() -> update(manager));
            return;
        }

        synchronized (manager) {
            try {
                var list = manager.getAllIngredients(VanillaTypes.ITEM_STACK).stream().filter(SHOULD_REMOVE).toList();
                if (list.size() > 0) {
                    manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, list);
                }

                for (var group : ItemGroup.GROUPS) {
                    if (group == ItemGroup.SEARCH) {
                        continue;
                    }

                    Collection<ItemStack> stacks;

                    if (group instanceof InternalClientItemGroup clientItemGroup) {
                        stacks = clientItemGroup.getStacks();
                    } else {
                        stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
                    }

                    if (stacks != null && !stacks.isEmpty()) {
                        manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Identifier getPluginUid() {
        return ID;
    }
}
