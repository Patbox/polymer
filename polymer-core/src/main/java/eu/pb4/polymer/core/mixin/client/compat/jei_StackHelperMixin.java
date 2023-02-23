package eu.pb4.polymer.core.mixin.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import mezz.jei.common.util.StackHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(StackHelper.class)
public class jei_StackHelperMixin {
    @Inject(method = "getRegistryNameForStack", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void polymer$changeId(ItemStack stack, CallbackInfoReturnable<String> cir) {
        var id = PolymerItemUtils.getServerIdentifier(stack);
        if (id != null) {
            cir.setReturnValue(id.toString());
        }
    }
}
