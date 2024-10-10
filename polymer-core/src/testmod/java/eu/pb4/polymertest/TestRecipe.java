package eu.pb4.polymertest;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookGroup;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.List;

public class TestRecipe implements Recipe<RecipeInput> {

    private final ItemStack output;

    public TestRecipe(ItemStack output) {
        this.output = output;
    }


    @Override
    public boolean matches(RecipeInput inventory, World world) {
        return false;
    }

    @Override
    public ItemStack craft(RecipeInput inventory, RegistryWrapper.WrapperLookup lookup) {
        return this.output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return TestMod.TEST_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return TestMod.TEST_RECIPE_TYPE;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of();
    }

    @Override
    public RecipeBookCategory getRecipeBookTab() {
        return RecipeBookCategories.CAMPFIRE;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<TestRecipe>, PolymerObject {
        @Override
        public MapCodec<TestRecipe> codec() {
            return ItemStack.CODEC.xmap(TestRecipe::new, TestRecipe::stack).fieldOf("item");
        }

        @Override
        public PacketCodec<RegistryByteBuf, TestRecipe> packetCodec() {
            return null;
        }
    }

    private ItemStack stack() {
        return this.output;
    }
}
