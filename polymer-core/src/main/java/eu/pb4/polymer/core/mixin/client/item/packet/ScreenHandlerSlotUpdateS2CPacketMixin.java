package eu.pb4.polymer.core.mixin.client.item.packet;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
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
    private void polymer$replaceItem(CallbackInfoReturnable<ItemStack> cir) {
        if (ClientUtils.isSingleplayer()) {
            cir.setReturnValue(PolymerItemUtils.getPolymerItemStack(cir.getReturnValue(), ClientUtils.getPlayer()));
        }
    }
}
