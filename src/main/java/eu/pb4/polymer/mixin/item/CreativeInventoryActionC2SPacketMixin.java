package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.item.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryActionC2SPacket.class)
public class CreativeInventoryActionC2SPacketMixin {
    @Inject(method = "getItemStack", at = @At("TAIL"), cancellable = true)
    private void replaceWithReal(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemHelper.getRealItemStack(cir.getReturnValue()));
    }
}
