package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/item/ItemStack$1", priority = 500)
public abstract class ItemStackPacketCodecMixin {

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private ItemStack polymer$replaceWithVanillaItem(ItemStack itemStack, @Local(argsOnly = true) RegistryByteBuf buf) {
        var player = PolymerUtils.getPlayerContext();
        return PolymerItemUtils.getPolymerItemStack(itemStack, PolymerUtils.getTooltipType(player), buf.getRegistryManager(), player);
    }
    @ModifyReturnValue(method = "decode(Lnet/minecraft/network/RegistryByteBuf;)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN", ordinal = 1))
    private ItemStack polymerCore$decodeItemStackServer(ItemStack stack, @Local(argsOnly = true) RegistryByteBuf buf) {
        return PolymerCommonUtils.isServerNetworkingThread() ? PolymerItemUtils.getRealItemStack(stack, buf.getRegistryManager()) : stack;
    }
}