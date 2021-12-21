package eu.pb4.polymer.mixin.item.packet;

import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PacketByteBuf.class, priority = 500)
public class PacketByteBufMixin {
    @ModifyVariable(method = "writeItemStack", at = @At("HEAD"), ordinal = 0)
    private ItemStack polymer_replaceWithVanillaItem(ItemStack itemStack) {
        return PolymerItemUtils.getPolymerItemStack(itemStack, PolymerUtils.getPlayer());

    }

    @Environment(EnvType.SERVER)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceWithRealItem(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(PolymerItemUtils.getRealItemStack(cir.getReturnValue()));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer_replaceWithRealItemClient(CallbackInfoReturnable<ItemStack> cir) {
        if (PolymerUtils.isOnPlayerNetworking() && !ClientUtils.isClientSide()) {
            cir.setReturnValue(PolymerItemUtils.getRealItemStack(cir.getReturnValue()));
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "readItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;byRawId(I)Lnet/minecraft/item/Item;"))
    private Item polymer_replaceWithId(int id) {
        if (InternalClientRegistry.enabled && InternalClientRegistry.stable && InternalClientRegistry.itemsMatch) {
            var item = InternalClientRegistry.ITEMS.get(id);

            if (item != null && item.realServerItem() != null) {
                return item.realServerItem();
            }
        }

        return Item.byRawId(id);
    }
}