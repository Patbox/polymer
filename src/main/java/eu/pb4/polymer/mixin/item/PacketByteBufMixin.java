package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.PolymerUtils;
import eu.pb4.polymer.item.ItemHelper;
import eu.pb4.polymer.other.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(value = PacketByteBuf.class, priority = 500)
public class PacketByteBufMixin {
    @ModifyVariable(method = "writeItemStack", at = @At("HEAD"), ordinal = 0)
    private ItemStack polymer_replaceWithVanillaItem(ItemStack itemStack) {
        var player = PolymerUtils.getPlayer();
        if (player != null) {
            return ItemHelper.getVirtualItemStack(itemStack, player);
        } else {
            return itemStack;
        }
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceWithRealItem(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemHelper.getRealItemStack(cir.getReturnValue()));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceWithRealItemClient(CallbackInfoReturnable<ItemStack> cir) {
        if (PolymerUtils.isOnPlayerNetworking()) {
            cir.setReturnValue(ItemHelper.getRealItemStack(cir.getReturnValue()));
        }
    }
}