package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.item.ItemHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;


@Mixin(InventoryS2CPacket.class)
public class InventoryS2CPacketMixin {
    @Environment(EnvType.CLIENT)
    @Inject(method = "getContents", at = @At("RETURN"), cancellable = true)
    private void replaceItemsWithVirtualOnes(CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> list = new ArrayList<>();
        ServerPlayerEntity player = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid());

        for (ItemStack stack : cir.getReturnValue()) {
            list.add(ItemHelper.getVirtualItemStack(stack, player));
        }

        cir.setReturnValue(list);
    }
}
