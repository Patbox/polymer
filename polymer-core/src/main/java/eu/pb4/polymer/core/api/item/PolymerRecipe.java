package eu.pb4.polymer.core.api.item;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

/**
 * Interface used for creation of server-side recipes
 */
public interface PolymerRecipe extends PolymerSyncedObject<Recipe<?>> {
    @Nullable
    @Override
    default Recipe<?> getPolymerReplacement(PacketContext context) {
        return null;
    }
}
