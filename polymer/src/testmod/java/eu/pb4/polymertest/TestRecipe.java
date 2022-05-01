package eu.pb4.polymertest;

import com.google.gson.JsonObject;
import eu.pb4.polymer.api.item.PolymerRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TestRecipe implements Recipe<Inventory>, PolymerRecipe {

    private final Identifier id;
    private final ItemStack output;

    public TestRecipe(Identifier id, ItemStack output) {
        this.id = id;
        this.output = output;
    }

    @Override
    public Recipe<?> getPolymerRecipe(Recipe<?> input, ServerPlayerEntity player) {
        return PolymerRecipe.createStonecuttingRecipe(input);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return this.output;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TestMod.TEST_RECIPE_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TestMod.TEST_RECIPE_TYPE;
    }

    public static class Serializer implements RecipeSerializer<TestRecipe> {

        @Override
        public TestRecipe read(Identifier id, JsonObject json) {
            ItemStack output = ShapedRecipe.outputFromJson(json.getAsJsonObject("output"));
            return new TestRecipe(id, output);
        }

        @Override
        public TestRecipe read(Identifier id, PacketByteBuf buf) {
            ItemStack output = buf.readItemStack();
            return new TestRecipe(id, output);
        }

        @Override
        public void write(PacketByteBuf buf, TestRecipe recipe) {
            buf.writeItemStack(recipe.output);
        }
    }
}
