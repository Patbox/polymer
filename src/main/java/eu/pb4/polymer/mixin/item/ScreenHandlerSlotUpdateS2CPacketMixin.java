package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.item.ItemHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;


@Mixin(ScreenHandlerSlotUpdateS2CPacket.class)
public class ScreenHandlerSlotUpdateS2CPacketMixin {
    @Environment(EnvType.CLIENT)
    @Inject(method = "getItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceItemsWithVirtualOnes(CallbackInfoReturnable<ItemStack> cir) {
        if (MinecraftClient.getInstance().getServer() != null) {
            ServerPlayerEntity player = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid());
            cir.setReturnValue(ItemHelper.getVirtualItemStack(cir.getReturnValue(), player));
        }
    }
}
