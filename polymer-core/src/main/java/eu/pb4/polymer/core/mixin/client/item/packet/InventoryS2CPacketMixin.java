package eu.pb4.polymer.core.mixin.client.item.packet;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(InventoryS2CPacket.class)
public class InventoryS2CPacketMixin {
    @Environment(EnvType.CLIENT)
    @Inject(method = "getContents", at = @At("RETURN"), cancellable = true)
    private void polymer$replaceItems(CallbackInfoReturnable<List<ItemStack>> cir) {
        if (ClientUtils.isSingleplayer()) {
            List<ItemStack> list = new ArrayList<>();
            ServerPlayerEntity player = ClientUtils.getPlayer();

            for (ItemStack stack : cir.getReturnValue()) {
                list.add(PolymerItemUtils.getPolymerItemStack(stack, player));
            }

            cir.setReturnValue(list);
        }
    }
}
