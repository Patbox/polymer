package eu.pb4.polymer.mixin.client.item;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyArg(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;literal(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", ordinal = 2))
    private String polymer_changeId(String id) {
        var identifier = PolymerItemUtils.getServerIdentifier((ItemStack) (Object) this);

        return identifier != null ? identifier.toString() : id;
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void polymer_changeStackSize(CallbackInfoReturnable<Integer> cir) {
        if (PolymerImpl.CHANGING_QOL_CLIENT && ClientUtils.isClientThread()) {
            var item = InternalClientRegistry.ITEMS.get(PolymerItemUtils.getPolymerIdentifier((ItemStack) (Object) this));

            if (item != null && item.stackSize() > 0) {
                cir.setReturnValue(item.stackSize());
            }
        }
    }
}
