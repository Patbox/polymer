package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/item/ItemStack$1", priority = 500)
public abstract class ItemStackPacketCodecMixin {

    @Unique
    private int polymer$readerIndex;

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0)
    private ItemStack polymer$replaceWithVanillaItem(ItemStack itemStack) {
        return PolymerItemUtils.getPolymerItemStack(itemStack, PolymerUtils.getPlayerContext());

    }

    //@Environment(EnvType.SERVER)
    @ModifyReturnValue(method = "decode(Lnet/minecraft/network/RegistryByteBuf;)Lnet/minecraft/item/ItemStack;", at = @At(value = "RETURN", ordinal = 1))
    private ItemStack polymerCore$decodeItemStackServer(ItemStack stack) {
        return PolymerCommonUtils.isServerNetworkingThread() ? PolymerItemUtils.getRealItemStack(stack) : stack;
    }

    /*@Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
    private void polymer$storeIndex(CallbackInfoReturnable<ItemStack> cir) {
        this.polymer$readerIndex = this.readerIndex();
    }

    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(method = "readItemStack", at = @At(value = "RETURN", ordinal = 1))
    private ItemStack polymerCore$decodeItemStackClient(ItemStack stack) {
        var currentIndex = this.readerIndex();
        this.readerIndex(this.polymer$readerIndex);
        var rawId = this.readVarInt();
        this.readerIndex(currentIndex);

        if (InternalClientRegistry.enabled && rawId != 0 && stack.getItem() == Items.AIR) {
            var item = InternalClientRegistry.ITEMS.get(rawId);

            if (item != null) {
                return item.visualStack().copyWithCount(stack.getCount());
            }
        } else if (PolymerCommonUtils.isServerNetworkingThread() && PolymerUtils.isSingleplayer()) {
            return PolymerItemUtils.getRealItemStack(stack);
        }

        return stack;
    }*/
}