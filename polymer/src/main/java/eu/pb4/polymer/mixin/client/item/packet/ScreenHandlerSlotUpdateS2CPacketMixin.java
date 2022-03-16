package eu.pb4.polymer.mixin.client.item.packet;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public class ScreenHandlerSlotUpdateS2CPacketMixin {
    @Environment(EnvType.CLIENT)
    @Inject(method = "getItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceItemsWithVirtualOnes(CallbackInfoReturnable<ItemStack> cir) {
        if (ClientUtils.isSingleplayer()) {
            cir.setReturnValue(PolymerItemUtils.getPolymerItemStack(cir.getReturnValue(), ClientUtils.getPlayer()));
        }
    }
}
