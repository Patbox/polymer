package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class JeiCompatibility implements IModPlugin {
    private static final Identifier ID = new Identifier("polymer", "jei_plugin");


    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (PolymerImpl.IS_CLIENT) {
            update(registration.getIngredientManager());
        }
    }

    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        CompatUtils.registerReload(() -> update(jeiRuntime.getIngredientManager()));
    }

    private static void update(IIngredientManager manager) {
        synchronized (manager) {
            try {
                var list = manager.getAllIngredients(VanillaTypes.ITEM_STACK).stream().filter(PolymerImplUtils::isPolymerControlled).toList();
                if (list.size() > 0) {
                    manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, list);
                }

                var stacks = new ArrayList<ItemStack>();
                CompatUtils.iterateItems(stacks::add);
                manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
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
