package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
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
    @ModifyReturnValue(method = "readItemStack", at = @At(value = "RETURN", ordinal = 1))
    private ItemStack polymerCore$decodeItemStackServer(ItemStack stack) {
        return (PolymerItemUtils.getRealItemStack(stack));
    }

    @Environment(EnvType.CLIENT)
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
    }
}