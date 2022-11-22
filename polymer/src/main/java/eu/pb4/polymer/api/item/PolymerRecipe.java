package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.utils.PolymerSyncedObject;
import net.minecraft.item.Items;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

/**
 * Interface used for creation of server-side recipes
 */
public interface PolymerRecipe extends PolymerSyncedObject<Recipe<?>> {
    /**
     * Returns client-side recipe used on client for specific player
     * This allows the client to still display Recipe Unlocked toast messages to the player.
     *
     * The provided methods for generating a recipe unsure that the
     * recipe will not appear in the incorrect recipe book screen.
     * @see PolymerRecipe#createBlastingRecipe(Recipe) - For a Blast Furnace Toast Icon
     * @see PolymerRecipe#createCraftingRecipe(Recipe) - For a Crafting Table Toast Icon
     * @see PolymerRecipe#createCampfireCookingRecipe(Recipe) - For a Campfire Toast Icon
     * @see PolymerRecipe#createSmeltingRecipe(Recipe) - For a Furnace Toast Icon
     * @see PolymerRecipe#createSmithingRecipe(Recipe) - For a Smithing Table Toast Icon
     * @see PolymerRecipe#createSmokingRecipe(Recipe) - For a Smoker Toast Icon
     * @see PolymerRecipe#createStonecuttingRecipe(Recipe) - For a Stonecutter Toast Icon
     *
     * @param player Player recipe is send to
     * @return Vanilla (or other) Recipe instance, or null if the recipe is hidden from the client
     */
    @Nullable
    @Override
    default Recipe<?> getPolymerReplacement(ServerPlayerEntity player) {
        return null;
    }

    /**
     * Make the client display as a {@link RecipeType#BLASTING}.
     * Icon Used: {@link Items#BLAST_FURNACE}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createBlastingRecipe(Recipe<?> input) {
        return new BlastingRecipe(input.getId(),"impossible", CookingRecipeCategory.MISC, Ingredient.EMPTY, input.getOutput(), 0, 0);
    }

    /**
     * Make the client display as a {@link RecipeType#CRAFTING}.
     * Icon Used: {@link Items#CRAFTING_TABLE}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createCraftingRecipe(Recipe<?> input) {
        return new ShapelessRecipe(input.getId(), "impossible", CraftingRecipeCategory.MISC, input.getOutput(), DefaultedList.of());
    }

    /**
     * Make the client display as a {@link RecipeType#CAMPFIRE_COOKING}.
     * Icon Used: {@link Items#CAMPFIRE}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createCampfireCookingRecipe(Recipe<?> input) {
        return new CampfireCookingRecipe(input.getId(), "impossible", CookingRecipeCategory.MISC, Ingredient.EMPTY, input.getOutput(), 0, 0);
    }

    /**
     * Make the client display as a {@link RecipeType#SMELTING}.
     * Icon Used: {@link Items#FURNACE}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createSmeltingRecipe(Recipe<?> input) {
        return new SmeltingRecipe(input.getId(), "impossible", CookingRecipeCategory.MISC, Ingredient.EMPTY, input.getOutput(), 0, 0);
    }

    /**
     * Make the client display as a {@link RecipeType#SMITHING}.
     * Icon Used: {@link Items#SMITHING_TABLE}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createSmithingRecipe(Recipe<?> input) {
        return new SmithingRecipe(input.getId(), Ingredient.EMPTY, Ingredient.EMPTY, input.getOutput());
    }

    /**
     * Make the client display as a {@link RecipeType#SMOKING}.
     * Icon Used: {@link Items#SMOKER}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createSmokingRecipe(Recipe<?> input) {
        return new SmokingRecipe(input.getId(), "impossible", CookingRecipeCategory.MISC, Ingredient.EMPTY, input.getOutput(), 0, 0);
    }

    /**
     * Make the client display as a {@link RecipeType#STONECUTTING}.
     * Icon Used: {@link Items#STONECUTTER}
     * @param input the Modded recipe
     * @return the Vanilla recipe
     */
    static Recipe<?> createStonecuttingRecipe(Recipe<?> input) {
        return new StonecuttingRecipe(input.getId(), "impossible", Ingredient.EMPTY, input.getOutput());
    }
}
