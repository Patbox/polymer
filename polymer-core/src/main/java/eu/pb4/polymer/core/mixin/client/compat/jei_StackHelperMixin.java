package eu.pb4.polymer.core.mixin.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
//import mezz.jei.common.util.StackHelper;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.common.util.StackHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Environment(EnvType.CLIENT)
@Mixin(StackHelper.class)
public class jei_StackHelperMixin {
    @Inject(method = "getUidForStack(Lnet/minecraft/world/item/ItemStack;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void polymer$changeId(ItemStack stack, UidContext context, CallbackInfoReturnable<Object> cir) {
        var id = CompatUtils.getKey(stack);
        if (id != null) {
            cir.setReturnValue(id);
        }
    }

    @Inject(method = "getUidForStack(Lmezz/jei/api/ingredients/ITypedIngredient;Lmezz/jei/api/ingredients/subtypes/UidContext;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void polymer$changeId2(ITypedIngredient<ItemStack> typedIngredient, UidContext context, CallbackInfoReturnable<Object> cir) {
        var id = CompatUtils.getKey(typedIngredient.getIngredient());
        if (id != null) {
            cir.setReturnValue(id);
        }
    }
}
