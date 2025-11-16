package eu.pb4.polymer.core.mixin.client.item.recipe;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(RecipeFinder.class)
public class RecipeFinderMixin {
    @Inject(method = "addInput(Lnet/minecraft/item/ItemStack;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeMatcher;add(Ljava/lang/Object;I)V"))
    private void passStack(ItemStack item, int maxCount, CallbackInfo ci) {

    }
}
