package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import eu.pb4.polymer.core.impl.other.ComponentChangesMap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(TradedItem.class)
public class TradedItemMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, TradedItem> polymerifyTheStack(PacketCodec<RegistryByteBuf, TradedItem> original) {
        return new TransformingPacketCodec<>(original, (buf, tradedItem) -> {
            var input = tradedItem.itemStack();
            var stack = PolymerItemUtils.getPolymerItemStack(input, PacketContext.get());
            return stack != input ? new TradedItem(stack.getItem().getRegistryEntry(), stack.getCount(), ComponentMapPredicate.of(new ComponentChangesMap(stack.getComponentChanges()))) : tradedItem;
        }, (buf, tradedItem) -> {
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var input = tradedItem.itemStack();
                var stack = PolymerItemUtils.getRealItemStack(input, buf.getRegistryManager());
                return stack != input ? new TradedItem(stack.getItem().getRegistryEntry(), stack.getCount(), ComponentMapPredicate.of(new ComponentChangesMap(stack.getComponentChanges()))) : tradedItem;
            }
            return tradedItem;
        });
    }
}
