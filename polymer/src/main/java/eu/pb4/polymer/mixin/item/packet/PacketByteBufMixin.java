package eu.pb4.polymer.mixin.item.packet;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PacketByteBuf.class, priority = 500)
public abstract class PacketByteBufMixin {
    @Shadow public abstract int readerIndex();

    @Shadow public abstract int readVarInt();

    @Shadow public abstract ByteBuf readerIndex(int index);

    @Unique
    private int polymer_readerIndex;

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
    @Inject(method = "readItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
    private void polymer_replaceWithRealItemClient(CallbackInfoReturnable<ItemStack> cir) {
        this.polymer_readerIndex = this.readerIndex();
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "readItemStack", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void polymer_replaceWithRealItemClient(CallbackInfoReturnable<ItemStack> cir, Item decodedItem, int i, ItemStack itemStack) {
        ItemStack stack = cir.getReturnValue();
        var currentIndex = this.readerIndex();
        this.readerIndex(this.polymer_readerIndex);
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

    @Environment(EnvType.CLIENT)
    @Redirect(method = "readItemStack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;"))
    private Object polymer_replaceWithId(PacketByteBuf instance, IndexedIterable<?> registry) {
        return InternalClientRegistry.decodeItem(instance.readVarInt());
    }
}