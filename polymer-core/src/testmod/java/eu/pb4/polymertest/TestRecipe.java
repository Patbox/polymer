package eu.pb4.polymertest;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.item.PolymerRecipe;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class TestRecipe implements Recipe<RecipeInput>, PolymerRecipe {

    private final ItemStack output;

    public TestRecipe(ItemStack output) {
        this.output = output;
    }

    @Override
    public Recipe<?> getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerRecipe.createStonecuttingRecipe(this);
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
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup lookup) {
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
