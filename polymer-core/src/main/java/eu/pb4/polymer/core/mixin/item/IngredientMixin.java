package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.IngredientExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = Ingredient.class, priority = 1200)
public class IngredientMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;", ordinal = 0))
    private static StreamCodec<RegistryFriendlyByteBuf, Ingredient> modifyRegularCodec(StreamCodec<RegistryFriendlyByteBuf, Ingredient> original) {
        if (!PolymerImpl.EXTENDED_RECIPE_INGREDIENTS) {
            return original;
        }

        return new IngredientExtension.BaseStreamCodec(original);
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;", ordinal = 1))
    private static StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> modifyOptionalCodec(StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> original) {
        if (!PolymerImpl.EXTENDED_RECIPE_INGREDIENTS) {
            return original;
        }
        return new IngredientExtension.OptionalStreamCodec(original);
    }
}
