package eu.pb4.polymer.core.mixin.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.ingredients.subtypes.SubtypeManager;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(SubtypeManager.class)
public class jei_SubtypeManagerMixin {
    @Inject(method = "getSubtypeInfo", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer_handlePolymerSubtypes(IIngredientTypeWithSubtypes<?, ?> ingredientType, Object ingredient, UidContext context, CallbackInfoReturnable<@Nullable String> cir) {
        if (ingredient instanceof ItemStack stack) {
            var id = PolymerItemUtils.getPolymerIdentifier(stack);
            if (id != null) {
                cir.setReturnValue("polymer_item|" + id);
            }
        }
    }
}
