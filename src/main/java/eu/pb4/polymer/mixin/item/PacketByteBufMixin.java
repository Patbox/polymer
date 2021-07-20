package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.item.ItemHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(value = PacketByteBuf.class, priority = 500)
public class PacketByteBufMixin {
    @ModifyVariable(method = "writeItemStack", at = @At("HEAD"), ordinal = 0)
    private ItemStack replaceWithVanillaItem(ItemStack itemStack) {
        return ItemHelper.getVirtualItemStack(itemStack, PacketContext.get().getTarget());
    }

    @Environment(EnvType.SERVER)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void replaceWithRealItem(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemHelper.getRealItemStack(cir.getReturnValue()));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void replaceWithRealItemClient(CallbackInfoReturnable<ItemStack> cir) {
        if (MinecraftClient.getInstance().getServer() != null && PacketContext.get().getTarget() != null) {
            cir.setReturnValue(ItemHelper.getRealItemStack(cir.getReturnValue()));
        }
    }
}