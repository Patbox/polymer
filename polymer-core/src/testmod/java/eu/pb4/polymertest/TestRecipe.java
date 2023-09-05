package eu.pb4.polymertest;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.item.PolymerRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TestRecipe implements Recipe<Inventory>, PolymerRecipe {

    private final ItemStack output;

    public TestRecipe(ItemStack output) {
        this.output = output;
    }

    @Override
    public Recipe<?> getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerRecipe.createStonecuttingRecipe(this);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager dynamicRegistryManager) {
        return this.output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager dynamicRegistryManager) {
        return this.output;
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
        public Codec<TestRecipe> codec() {
            return ItemStack.CODEC.xmap(TestRecipe::new, TestRecipe::stack);
        }

        @Override
        public TestRecipe read(PacketByteBuf buf) {
            return new TestRecipe(buf.readItemStack());
        }

        @Override
        public void write(PacketByteBuf buf, TestRecipe recipe) {
            buf.writeItemStack(recipe.output);
        }
    }

    private ItemStack stack() {
        return this.output;
    }
}
