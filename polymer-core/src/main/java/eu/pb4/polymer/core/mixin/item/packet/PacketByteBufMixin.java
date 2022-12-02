package eu.pb4.polymer.core.mixin.item.packet;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PacketByteBuf.class, priority = 500)
public abstract class PacketByteBufMixin {
    @Shadow public abstract int readerIndex();

    @Shadow public abstract int readVarInt();

    @Shadow public abstract ByteBuf readerIndex(int index);

    @Unique
    private int polymer$readerIndex;

    @ModifyVariable(method = "writeItemStack", at = @At("HEAD"), ordinal = 0)
    private ItemStack polymer$replaceWithVanillaItem(ItemStack itemStack) {
        return PolymerItemUtils.getPolymerItemStack(itemStack, PolymerUtils.getPlayerContext());

    }

    @Environment(EnvType.SERVER)
    @Inject(method = "readItemStack", at = @At("RETURN"), cancellable = true)
    private void polymer$decodeItemStackServer(CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(PolymerItemUtils.getRealItemStack(cir.getReturnValue()));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
    private void polymer$storeIndex(CallbackInfoReturnable<ItemStack> cir) {
        this.polymer$readerIndex = this.readerIndex();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void polymer$decodeItemStackClient(CallbackInfoReturnable<ItemStack> cir, Item decodedItem, int i, ItemStack itemStack) {
        ItemStack stack = cir.getReturnValue();
        var currentIndex = this.readerIndex();
        this.readerIndex(this.polymer$readerIndex);
        var rawId = this.readVarInt();
        this.readerIndex(currentIndex);

        if (InternalClientRegistry.enabled && rawId != 0 && stack.getItem() == Items.AIR) {
            var item = InternalClientRegistry.ITEMS.get(rawId);

            if (item != null) {
                var count = stack.getCount();

                stack = item.visualStack().copy();
                stack.setCount(count);

                cir.setReturnValue(stack);
            }
        } else if (PolymerUtils.isOnPlayerNetworking() && !ClientUtils.isClientThread()) {
            cir.setReturnValue(PolymerItemUtils.getRealItemStack(stack));
        }
    }
}